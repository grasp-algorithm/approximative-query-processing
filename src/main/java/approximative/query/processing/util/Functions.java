package approximative.query.processing.util;

/**
 * @author LIRIS
 * @version 1.0
 * @since 1.0 6/21/18.
 */
public class Functions {

    private Functions() {
        throw new UnsupportedOperationException("This class couldn't be instatiated.");
    }

    public static int nCr(int n, int r) {
        if (r == 0 || n == r)
            return 1;
        else
            return nCr(n - 1, r) + nCr(n - 1, r - 1);
    }

    public static long comb(int n, int r) {
        long numerator = 1;
        long denominator = 1;

        if (r > n - r) {
            r = n - r;
        }
        for (long i = 1L; i <= r; ++i) {
            denominator *= i;
        }
        for (long i = n - r + 1L; i <= n; ++i) {
            numerator *= i;
        }
        return numerator / denominator;
    }



}
