package icyllis.modernui.mc;

/**
 * Implemented by the Gui mixin so other mixins can coordinate the single screen-change
 * dispatch.
 * <p>
 * This lives outside the mixin package on purpose: a class inside it belongs to the mixin
 * config and may not be referenced directly, which fails at class load time.
 */
public interface GuiDispatchGuard {

    /** Marks the change currently being reported as already handled. */
    void modernUI_MC$suppressNextDispatch();

    /** Ends the suppression above. */
    void modernUI_MC$restoreDispatch();
}
