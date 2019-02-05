package nl.tabuu.hopperx.hopper;

/**
 * This enum can be used to define a hoppers position relative to an inventory/container its position.
 * The order of this enum is important:
 *  - The top 3 values will be used as possible hopper push destinations
 *  - The order of the top 3 values will be used as the priority.
 */
public enum XHopperDestination {
    LINKED,
    BELOW,
    ADJACENT,
    ABOVE,
    BLACKLIST,
    UNKNOWN
}
