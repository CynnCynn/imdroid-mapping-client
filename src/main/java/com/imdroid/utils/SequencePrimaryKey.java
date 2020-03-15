package com.imdroid.utils;

import com.imdroid.pojo.bo.BusinessException;

/**
 * System sequence number generator.
 */
public class SequencePrimaryKey {
    private static final int base = 100000;

    private static long millis = 0, old = 0;

    /**
     * Get the base Sequence
     *
     * @return The system sequence number.
     * @throws Exception
     */
    public static synchronized long getSequence() throws BusinessException {
        long r = System.currentTimeMillis();

        if (r == millis) {
            old++;

            if (old >= base)
                throw new BusinessException("Get Sequence error.");

            r = millis * base + old;
        } else {
            millis = r;
            r *= base;
            old = 0;
        }
        return r;
    }
}