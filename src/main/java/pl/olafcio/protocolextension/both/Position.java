package pl.olafcio.protocolextension.both;

import org.jetbrains.annotations.Range;

public record Position(
        @Range(from = 0, to = 1) double x,
        @Range(from = 0, to = 1) double y
) {
    public static final Position ZERO = new Position(0, 0);

    public boolean isWithin(double x, double y, double width, double height) {
        return this.x >= x && this.y >= y && this.x <= x + width && this.y <= y + height;
    }
}
