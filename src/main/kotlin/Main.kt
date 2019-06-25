import kotlinx.coroutines.Dispatchers.Unconfined
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.rx2.rxFlowable

/**
 * This function should repo the bug by _not_ crashing when the [LinkageError] is thrown.
 *
 * Regular exceptions crash the program, but any exception considered fatal by
 * [io.reactivex.exceptions.Exceptions.throwIfFatal] will get swallowed.
 */
@ExperimentalCoroutinesApi
fun main() {
    Thread.setDefaultUncaughtExceptionHandler { _, e ->
        // This does not get executed at all either.
        e.printStackTrace()
    }

    // Using rxObservable will cause the program to crash as expected.
    val flowable = GlobalScope.rxFlowable(Unconfined) {
        try {
            println("1: Sending value to subscriber.")
            send(Unit)
        } catch (e: LinkageError) {
            println("3: Caught the LinkageError from the rxFlowable builder, rethrowing…")
            println("   This *should* result in an OnErrorNotImplementedException, but doesn't.")
            throw e
        }
    }

    flowable
        .publish() // replay(1) works here too.
        .refCount() // autoConnect() or manually connecting works here too.
        .subscribe {
            println("2: Throwing exception from subscriber.")
            // If you change this to throw RuntimeException() (or any exception not considered fatal by RxJava2),
            // the program crashes as expected.
            throw LinkageError()
        }

    println("4: Returning from main, without crashing.")
}
