package merkulyevsasha.ru.processors;

public class Values{
    public String stringValue;
    public int intValue;
    public float floatValue;
    public short shortValue;
    public long longValue;
    public double doubleValue;
    public boolean booleanValue;
    public byte byteValue;

    public Values() {
        stringValue = "";
    }

    public Values(String stringValue, int intValue, float floatValue, short shortValue, long longValue, double doubleValue, boolean booleanValue, byte byteValue) {
        this.stringValue = stringValue;
        this.intValue = intValue;
        this.floatValue = floatValue;
        this.shortValue = shortValue;
        this.longValue = longValue;
        this.doubleValue = doubleValue;
        this.booleanValue = booleanValue;
        this.byteValue = byteValue;
    }
}
