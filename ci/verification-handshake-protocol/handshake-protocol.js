work = load("call-final-a12.stg.work");
write(
    "Handshake protocol checks:\n" +
    "  * incorrect expression: " + checkStgHandshakeProtocol(work, "incorrect - expression") + "\n" +
    "  * {incorrect}{signals}: " + checkStgHandshakeProtocol(work, "{incorrect} {signals}") + "\n" +
    "  * {a}{r}: " + checkStgHandshakeProtocol(work, "{a}{r}") + "\n" +
    "  * {  a  }  {  r  }: " + checkStgHandshakeProtocol(work, "  {  a  }  {  r  }  ") + "\n" +
    "  * {r} {a}: " + checkStgHandshakeProtocol(work, "{r} {a}") + "\n" +
    "  * {r1 r2} {a12}: " + checkStgHandshakeProtocol(work, "{r1 r2} {a12}") + "\n" +
    "  * {a12} {r1 r2}: " + checkStgHandshakeProtocol(work, "{a12} {r1 r2}") + "\n" +
    "  * {a12} {r1 r2}, REQ1ACK0, allow-inversion=false: " + checkStgHandshakeProtocol(work,
        "<settings state=\"REQ1ACK0\" allow-inversion=\"false\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>") + "\n" +
    "  * {a12} -> {r1 r2}, REQ1ACK0, allow-inversion=true: " + checkStgHandshakeProtocol(work,
        "<settings state=\"REQ1ACK0\" allow-inversion=\"true\"><req name=\"a12\"/><ack name=\"r1\"/><ack name=\"r2\"/></settings>") + "\n" +
    "", "handshake-protocol.result");
exit();
