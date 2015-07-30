package com.yy.misaka.demo.util;

import android.util.Log;

/**
 * Custom handler for any uncaught exceptions.
 * <p/>
 * <P>By default, a Swing app will handle uncaught exceptions simply by
 * printing a stack trace to {@link System#err}. However, the end user will
 * not usually see that, and if they do, they will not likely understand it.
 * This class addresses that problem, by showing the end user a
 * simple error message in a modal dialog. (The dialog's owner is the
 * currently active frame.)
 */
public final class ExceptionHandler implements Thread.UncaughtExceptionHandler {

    /**
     * Custom handler for uncaught exceptions.
     * <p/>
     * <P>Displays a simple model dialog to the user, showing that an error has
     * occured. The text of the error includes {@link Throwable#toString()}.
     * The stack trace is logged at a SEVERE level.
     */
    @Override
    public void uncaughtException(Thread aThread, Throwable aThrowable) {
        Log.e("Misaka", "crashed", aThrowable);
//        throw aThrowable;
    }

}