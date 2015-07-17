package net.coding.program.login.auth;

import android.net.Uri;
import android.util.Log;

import java.io.Serializable;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by chenchao on 15/7/1.
 */
public class AuthInfo implements Serializable {

    public static final String LOCAL_TAG = "AuthInfo";
    public static final String PARAM_SECRET = "secret";
    private static final String PARAM_ISSUER = "issuer";
    private static final TotpCounter mTotpCounter = new TotpCounter(PasscodeGenerator.INTERVAL);
    private final String scheme;
    private final String path;
    private final String authority;
    private final String issuer;
    private final String secret;
    TotpClock clock;
    TotpCounter counter = new TotpCounter(PasscodeGenerator.INTERVAL);
    private String uriString;

    public AuthInfo(String uriString, TotpClock clock) {
        this.uriString = uriString;

        Uri uri = Uri.parse(uriString);
        scheme = uri.getScheme();
        path = uri.getPath();
        authority = uri.getAuthority();
        issuer = uri.getQueryParameter(PARAM_ISSUER);
        this.secret = uri.getQueryParameter(PARAM_SECRET);
        this.clock = clock;
    }

    public static boolean isAuthUrl(String uriString) {
        Uri uri = Uri.parse(uriString);
        return uri.getScheme() != null &&
                uri.getPath() != null &&
                uri.getAuthority() != null &&
                uri.getQueryParameter(PARAM_ISSUER) != null &&
                uri.getQueryParameter(PARAM_SECRET) != null &&
                "totp".equals(uri.getAuthority());
    }

    public static TotpCounter getTotpCountet() {
        return mTotpCounter;
    }

    static Signer getSigningOracle(String secret) {
        try {
            byte[] keyBytes = decodeKey(secret);
            final Mac mac = Mac.getInstance("HMACSHA1");
            mac.init(new SecretKeySpec(keyBytes, ""));

            // Create a signer object out of the standard Java MAC implementation.
            return new Signer() {
                @Override
                public byte[] sign(byte[] data) {
                    return mac.doFinal(data);
                }
            };
        } catch (Base32String.DecodingException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        } catch (NoSuchAlgorithmException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        } catch (InvalidKeyException error) {
            Log.e(LOCAL_TAG, error.getMessage());
        }

        return null;
    }

    private static byte[] decodeKey(String secret) throws Base32String.DecodingException {
        return Base32String.decode(secret);
    }

    public String getUriString() {
        return uriString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthInfo info = (AuthInfo) o;

        if (scheme != null ? !scheme.equals(info.scheme) : info.scheme != null) return false;
        if (path != null ? !path.equals(info.path) : info.path != null) return false;
        if (authority != null ? !authority.equals(info.authority) : info.authority != null)
            return false;
        if (issuer != null ? !issuer.equals(info.issuer) : info.issuer != null) return false;
        return !(secret != null ? !secret.equals(info.secret) : info.secret != null);

    }

    @Override
    public int hashCode() {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + (authority != null ? authority.hashCode() : 0);
        result = 31 * result + (issuer != null ? issuer.hashCode() : 0);
        result = 31 * result + (secret != null ? secret.hashCode() : 0);
        return result;
    }

    // 其它的都相同，只有密钥不同
    public boolean equalsAccount(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthInfo info = (AuthInfo) o;

        if (scheme != null ? !scheme.equals(info.scheme) : info.scheme != null) return false;
        if (path != null ? !path.equals(info.path) : info.path != null) return false;
        if (authority != null ? !authority.equals(info.authority) : info.authority != null)
            return false;
        if (issuer != null ? !issuer.equals(info.issuer) : info.issuer != null) return false;
        return !(secret != null ? secret.equals(info.secret) : info.secret != null);
    }

    public String getCode() {
        String code = "";
        try {
            Signer signer = getSigningOracle(secret);
            long state = counter.getValueAtTime(Utilities.millisToSeconds(clock.currentTimeMillis()));

            PasscodeGenerator pcg = new PasscodeGenerator(signer);
            code = pcg.generateResponseCode(state);
        } catch (Exception e) {
        }

        return code;
    }

    public String getCompany() {
        return issuer;
    }

    public String getAccountName() {
        String name = path;
        if (path.startsWith("/")) {
            name = path.substring(1);
        }

        return name;
    }
}
