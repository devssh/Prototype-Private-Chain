package app.model;

public class HashString {
    public final String str;

    public HashString(String str) {
        this.str = str;
    }

    public static HashString hashString(String str) {
        return new HashString(str);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HashString that = (HashString) o;

        return str != null ? str.equals(that.str) : that.str == null;
    }

    @Override
    public int hashCode() {
        return str != null ? str.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "HashString{" +
                "str='" + str + '\'' +
                '}';
    }
}
