package moondolphin.miportafolio.asistente;

enum AssistantTool {
    disponibilidad(false), consultar_solicitud(false), registrar(true), reintentar(true);

    private final boolean effect;
    AssistantTool(boolean effect) { this.effect = effect; }
    boolean hasEffect() { return effect; }
    static AssistantTool named(String name) {
        try { return valueOf(name); } catch (IllegalArgumentException exception) { return null; }
    }
}
