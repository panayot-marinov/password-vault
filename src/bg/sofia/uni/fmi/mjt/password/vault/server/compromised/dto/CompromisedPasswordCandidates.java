package bg.sofia.uni.fmi.mjt.password.vault.server.compromised.dto;

import com.google.gson.annotations.SerializedName;

import java.util.Collection;

public class CompromisedPasswordCandidates {

    @SerializedName("candidates")
    private final Collection<CompromisedPassword> compromisedPasswordCandidates;

    public CompromisedPasswordCandidates(Collection<CompromisedPassword> compromisedPasswordCandidates) {
        this.compromisedPasswordCandidates = compromisedPasswordCandidates;
    }

    public Collection<CompromisedPassword> getAsCollection() {
        return compromisedPasswordCandidates;
    }

}