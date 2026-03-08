package gregtech.client.utils.formula;

import gregtech.api.unification.material.Material;

import org.jetbrains.annotations.Nullable;

public final class MaterialCharSequence {

    private final char[] chars;
    private final Material[] materials;
    private final int length;

    private MaterialCharSequence(char[] chars, Material[] materials, int length) {
        this.chars = chars;
        this.materials = materials;
        this.length = length;
    }

    public int length() {
        return length;
    }

    public char charAt(int index) {
        return chars[index];
    }

    @Nullable
    public Material materialAt(int index) {
        if (index < 0 || index >= length) return null;
        return materials[index];
    }

    public String getText() {
        return new String(chars, 0, length);
    }

    public boolean visit(MaterialCharSink sink) {
        for (int i = 0; i < length; i++) {
            if (!sink.accept(i, chars[i], materials[i])) return false;
        }
        return true;
    }

    public static class Builder {

        private char[] chars;
        private Material[] materials;
        private int size;

        public Builder(int cap) {
            chars = new char[Math.max(cap, 8)];
            materials = new Material[chars.length];
        }

        public Builder append(char c, @Nullable Material mat) {
            grow(size + 1);
            chars[size] = c;
            materials[size] = mat;
            size++;
            return this;
        }

        public Builder append(String s, @Nullable Material mat) {
            grow(size + s.length());
            for (int i = 0; i < s.length(); i++) {
                chars[size] = s.charAt(i);
                materials[size] = mat;
                size++;
            }
            return this;
        }

        private void grow(int min) {
            if (min <= chars.length) return;
            int cap = Math.max(chars.length * 2, min);
            char[] nc = new char[cap];
            Material[] nm = new Material[cap];
            System.arraycopy(chars, 0, nc, 0, size);
            System.arraycopy(materials, 0, nm, 0, size);
            chars = nc;
            materials = nm;
        }

        public int length() {
            return size;
        }

        public MaterialCharSequence build() {
            char[] c = new char[size];
            Material[] m = new Material[size];
            System.arraycopy(chars, 0, c, 0, size);
            System.arraycopy(materials, 0, m, 0, size);
            return new MaterialCharSequence(c, m, size);
        }
    }
}
