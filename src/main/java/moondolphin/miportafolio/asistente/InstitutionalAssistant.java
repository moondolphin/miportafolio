package moondolphin.miportafolio.asistente;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
class InstitutionalAssistant {
    private static final Pattern VERSION = Pattern.compile("(?m)^Versión ([\\d.]+)\\.");
    private static final Pattern HEADING = Pattern.compile("\\A(INST-\\d{2,})[ \\t]+[—–-][ \\t]+([^\\r\\n]+)(?:\\r?\\n|\\z)");

    private final OpenAiClient openAi;
    private final String prompt;
    private final List<Fragment> fragments;
    private volatile List<IndexedFragment> index;

    InstitutionalAssistant(OpenAiClient openAi) throws IOException {
        this.openAi = openAi;
        prompt = resource("system-prompt-v4.txt");
        fragments = parse(resource("base_rag_consultorio_v1.md"));
    }

    String prompt() {
        return prompt;
    }

    List<String> topics() {
        return fragments.stream().map(Fragment::title).toList();
    }

    Retrieval retrieve(String question) {
        var indexed = getIndex();
        var query = openAi.embed(List.of(question))[0];
        var matches = indexed.stream()
                .sorted(Comparator.<IndexedFragment>comparingDouble(entry -> cosine(entry.vector(), query)).reversed())
                .limit(3).map(IndexedFragment::fragment).toList();
        var sources = matches.stream().map(fragment -> new Source(fragment.id(), fragment.version())).toList();
        return new Retrieval(matches.stream().map(Fragment::context).collect(java.util.stream.Collectors.joining("\n\n---\n\n")),
                sources.get(0), sources);
    }

    ChatReply answer(String question) {
        var retrieved = retrieve(question);
        return new ChatReply(openAi.answer(prompt, retrieved.context(), question), retrieved.source());
    }

    record Retrieval(String context, Source source, List<Source> sources) {
        Retrieval(String context, Source source) { this(context, source, List.of(source)); }
    }

    private List<IndexedFragment> getIndex() {
        var cached = index;
        return cached == null ? loadIndex() : cached;
    }

    private synchronized List<IndexedFragment> loadIndex() {
        if (index == null) {
            var vectors = openAi.embed(fragments.stream().map(Fragment::context).toList());
            var entries = new ArrayList<IndexedFragment>(fragments.size());
            for (int i = 0; i < fragments.size(); i++) {
                entries.add(new IndexedFragment(fragments.get(i), vectors[i]));
            }
            index = List.copyOf(entries);
        }
        return index;
    }

    private static double cosine(double[] left, double[] right) {
        if (left.length != right.length) {
            throw OpenAiClient.unavailable();
        }
        // The OpenAI client normalizes both vectors, so the dot product is cosine similarity.
        double similarity = 0;
        for (int i = 0; i < left.length; i++) {
            similarity += left[i] * right[i];
        }
        return similarity;
    }

    private static List<Fragment> parse(String markdown) {
        var version = VERSION.matcher(markdown);
        if (!version.find()) {
            throw new IllegalStateException("Falta la versión del corpus institucional.");
        }
        String[] sections = markdown.split("(?m)^##\\s+");
        var fragments = new ArrayList<Fragment>();
        var ids = new java.util.HashSet<String>();
        for (int i = 1; i < sections.length; i++) {
            var heading = HEADING.matcher(sections[i]);
            if (!heading.find()) {
                throw new IllegalStateException(
                        "Encabezado institucional inválido. Usá ## INST-01 — Título.");
            }
            String id = heading.group(1);
            String title = heading.group(2).strip();
            String body = sections[i].substring(heading.end()).strip();
            if (!ids.add(id) || title.isEmpty() || body.isEmpty()) {
                throw new IllegalStateException(
                        "Sección institucional inválida: " + id + ". Revisá IDs duplicados, título y contenido.");
            }
            fragments.add(new Fragment(id, version.group(1), title, body));
        }
        if (fragments.isEmpty()) {
            throw new IllegalStateException("El corpus debe contener al menos una sección institucional.");
        }
        return List.copyOf(fragments);
    }

    private static String resource(String name) throws IOException {
        return new ClassPathResource("asistente/" + name).getContentAsString(StandardCharsets.UTF_8);
    }

    record ChatReply(String answer, Source retrieved, List<AvailabilityClient.Slot> horarios,
                     BookingService.View booking, BookingConversation.Context conversation,
                     List<BookingConversation.Evidence> herramientas) {
        ChatReply(String answer, Source retrieved, List<AvailabilityClient.Slot> horarios,
                  BookingService.View booking, BookingConversation.Context conversation) {
            this(answer, retrieved, horarios, booking, conversation, List.of());
        }
        ChatReply(String answer, Source retrieved, List<AvailabilityClient.Slot> horarios) {
            this(answer, retrieved, horarios, null, null);
        }
        ChatReply(String answer, Source retrieved) {
            this(answer, retrieved, null);
        }
    }
    record Source(String id, String version) {}

    private record IndexedFragment(Fragment fragment, double[] vector) {}

    private record Fragment(String id, String version, String title, String body) {
        String context() {
            return "%s (v%s) — %s%n%n%s".formatted(id, version, title, body);
        }
    }
}
