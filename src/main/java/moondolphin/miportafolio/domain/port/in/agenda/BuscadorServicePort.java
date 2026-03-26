package moondolphin.miportafolio.domain.port.in.agenda;

import moondolphin.miportafolio.domain.model.agenda.JournalEntry;
import moondolphin.miportafolio.domain.model.agenda.LinkItem;
import moondolphin.miportafolio.domain.model.agenda.NotaLibre;
import moondolphin.miportafolio.domain.model.agenda.Tarea;
import java.util.List;

public interface BuscadorServicePort {
    BuscadorServicePort.Resultado buscar(String texto);

    class Resultado {
        private List<Tarea> tareas;
        private List<NotaLibre> notas;
        private List<JournalEntry> journal;
        private List<LinkItem> links;

        public Resultado(List<Tarea> tareas, List<NotaLibre> notas,
                         List<JournalEntry> journal, List<LinkItem> links) {
            this.tareas = tareas;
            this.notas = notas;
            this.journal = journal;
            this.links = links;
        }

        public List<Tarea> getTareas() { return tareas; }
        public List<NotaLibre> getNotas() { return notas; }
        public List<JournalEntry> getJournal() { return journal; }
        public List<LinkItem> getLinks() { return links; }
    }
}
