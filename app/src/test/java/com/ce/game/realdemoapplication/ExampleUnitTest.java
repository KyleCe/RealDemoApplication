package com.ce.game.realdemoapplication;

import org.junit.Test;

import java.util.Vector;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    private static Vector sVector = new Vector();

    @Test
    public void name() throws Exception {
        for (int i = 1; i < 100000000; i++) {
            Object o = new Object();
            sVector.add(o);
            o = null;
        }
    }
}