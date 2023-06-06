package bg.sofia.uni.fmi.mjt.password.vault.server.compromised.dto;

import bg.sofia.uni.fmi.mjt.password.vault.client.Password;
import com.google.gson.annotations.SerializedName;

import java.util.Objects;

public class CompromisedPassword implements Password {

    @SerializedName("md5")
    private final String md5Hash;
    @SerializedName("sha1")
    private final String sha1Hash;
    @SerializedName("sha256")
    private final String sha256Hash;
    private final boolean revealedInExposure;
    private final int relativeExposureFrequency;

    public CompromisedPassword(String md5Hash, String sha1Hash, String sha256Hash,
                               boolean revealedInExposure, int relativeExposureFrequency) {
        this.md5Hash = md5Hash;
        this.sha1Hash = sha1Hash;
        this.sha256Hash = sha256Hash;
        this.revealedInExposure = revealedInExposure;
        this.relativeExposureFrequency = relativeExposureFrequency;
    }

    @Override
    public String getMd5() {
        return md5Hash;
    }

    @Override
    public String getSha1() {
        return sha1Hash;
    }

    @Override
    public String getSha256() {
        return sha256Hash;
    }

    public boolean isRevealedInExposure() {
        return revealedInExposure;
    }

    public int getRelativeExposureFrequency() {
        return relativeExposureFrequency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompromisedPassword that = (CompromisedPassword) o;
        return revealedInExposure == that.revealedInExposure &&
                relativeExposureFrequency == that.relativeExposureFrequency &&
                Objects.equals(md5Hash, that.md5Hash) &&
                Objects.equals(sha1Hash, that.sha1Hash) &&
                Objects.equals(sha256Hash, that.sha256Hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(md5Hash, sha1Hash, sha256Hash, revealedInExposure, relativeExposureFrequency);
    }

    @Override
    public String toString() {
        return "CompromisedPasswordCandidate{" +
                "md5Hash='" + md5Hash + '\'' +
                ", sha1Hash='" + sha1Hash + '\'' +
                ", sha256Hash='" + sha256Hash + '\'' +
                ", revealedInExposure=" + revealedInExposure +
                ", relativeExposureFrequency=" + relativeExposureFrequency +
                '}';
    }
}