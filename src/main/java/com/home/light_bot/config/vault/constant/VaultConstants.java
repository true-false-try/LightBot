package com.home.light_bot.config.vault.constant;

public class VaultConstants {

    public static final String PROTOCOL_HTTPS = "https";

    public static final String MOUNT = "ms-telegram-light-bot";
    public static final String SECRET_CONFIG = "test-config";

    public static final String URL_USERPASS_SUFFIX = "auth/userpass/login/%s";

    public static final String HEADER_VAULT_TOKEN = "X-Vault-Token";
    public static final String URL_CONFIG_SUFFIX = MOUNT + "/data/" + SECRET_CONFIG;

    public static final String VAULT_SECRETS = "vault-secrets";

    //Credentials
    public static final String VAULT_HOST = "VAULT_HOST";
    public static final String VAULT_PORT = "VAULT_PORT";
    public static final String VAULT_LOGIN = "VAULT_LOGIN";
    public static final String VAULT_PASSWORD = "VAULT_PASSWORD";
    public static final String VAULT_JKS_PATH = "VAULT_JKS_PATH";
    public static final String VAULT_JKS_PASSWORD = "VAULT_JKS_PASSWORD";

    //Exception
    public static final String VAULT_CREDENTIAL_EXCEPTION = "Vault credentials %s, %s, are not set in environment variables!";
    public static final String VAULT_HOST_PORT_EXCEPTION = "Vault host or port %s, %s, are not set in environment variables!";
    public static final String VAULT_JKS_EXCEPTION = "Vault certificate path %s or password %s, are not set in environment variables!";
}
