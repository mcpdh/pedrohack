package me.numenmc.pedrohack.systems;

public abstract class ActionModule extends Module {
    public ActionModule(String name, String description) {
        super(name, description);
    }

    public ActionModule(String name, String description, boolean patched) {
        super(name, description, patched);
    }

    public abstract void onExecute();

    @Override
    protected final void onEnable() {}

    @Override
    protected final void onDisable() {}

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = false;
        onExecute();
    }

    @Override
    public void setEnabledSilent(boolean enabled) {
        this.enabled = false;
    }
}
