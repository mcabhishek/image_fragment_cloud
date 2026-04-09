package org.example.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Fragmenter {
    public List<byte[]> split(byte[] data, int k) {
        List<byte[]> fragments = new ArrayList<>();
        int size = data.length / k;
        for (int i = 0; i < k; i++) {
            int start = i * size;
            int end = (i == k - 1) ? data.length : (i + 1) * size;
            fragments.add(Arrays.copyOfRange(data, start, end));
        }
        return fragments;
    }

    public byte[] merge(List<byte[]> fragments) {
        int totalLength = fragments.stream().mapToInt(f -> f.length).sum();
        byte[] result = new byte[totalLength];
        int pos = 0;
        for (byte[] f : fragments) {
            System.arraycopy(f, 0, result, pos, f.length);
            pos += f.length;
        }
        return result;
    }
}