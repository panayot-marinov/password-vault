package bg.sofia.uni.fmi.mjt.password.vault.client;

public interface Password {

    String getMd5();

    String getSha1();

    String getSha256();

}