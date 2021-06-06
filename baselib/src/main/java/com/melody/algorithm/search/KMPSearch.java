package com.melody.algorithm.search;

/**
 * KMP算法
 */
public class KMPSearch implements IByteSearch {



    @Override
    public int search(byte[] txt, byte[] pat) {
        return KMPSearch(txt, pat, getNext(pat));
    }


    private static int KMPSearch(byte[] txt, byte[] pat, int[] next) {
        int M = txt.length;
        int N = pat.length;
        int i = 0;
        int j = 0;
        while (i < M && j < N) {
            if (j == -1 || txt[i] == pat[j]) {
                i++;
                j++;
            } else {
                j = next[j];
            }
        }
        if (j == N)
            return i - j;
        else
            return -1;
    }

    private static int[] getNext(byte[] pat) {
        int[] next = new int[pat.length];
        int N = pat.length;
        next[0] = -1;
        int k = -1;
        int j = 0;
        while (j < N - 1) {
            if (k == -1 || pat[j] == pat[k]) {
                ++k;
                ++j;
                next[j] = k;
            } else
                k = next[k];
        }
        return next;
    }

}
