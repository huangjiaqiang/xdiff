package com.melody.algorithm.search;

/**
 * 暴力算法
 */
public class ForceSearch implements IByteSearch {
    @Override
    public int search(byte[] txt, byte[] pat) {
        int M = txt.length;
        int N = pat.length;
        for (int i = 0; i <= M - N; i++) {
            int j;
            for (j = 0; j < N; j++) {
                if (txt[i + j] != pat[j])
                    break;
            }
            if (j == N)
                return i;
        }
        return -1;
    }
}
