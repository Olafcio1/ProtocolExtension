package pl.olafcio.protocolextension.server.api;

public class PacketConstructionError extends RuntimeException {
    public PacketConstructionError(String e) {
        super(e);
    }
}
