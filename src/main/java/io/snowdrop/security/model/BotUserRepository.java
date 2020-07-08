package io.snowdrop.security.model;

import java.security.Provider;

import javax.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wildfly.security.password.WildFlyElytronPasswordProvider;

import io.quarkus.hibernate.orm.panache.PanacheRepository;

@ApplicationScoped
public class BotUserRepository implements PanacheRepository<BotUser> {

  private static final Logger LOGGER = LoggerFactory.getLogger(BotUserRepository.class);

  static final Provider ELYTRON_PROVIDER = new WildFlyElytronPasswordProvider();

  public BotUser findByUsername(String username) {
    return find("username", username).firstResult();
  }

  public BotUser createUser(final String username, final String password, final String role) {
    BotUser botUser;
    //    PasswordFactory passwordFactory = null;
    //    try {
    //      passwordFactory = PasswordFactory.getInstance(BCryptPassword.ALGORITHM_BCRYPT, ELYTRON_PROVIDER);
    //    } catch (NoSuchAlgorithmException e) {
    //      e.printStackTrace();
    //    }
    //
    //    int iterationCount = 10;
    //
    //    byte[] salt = new byte[BCryptPassword.BCRYPT_SALT_SIZE];
    //    SecureRandom random = new SecureRandom();
    //    random.nextBytes(salt);
    //
    //    IteratedSaltedPasswordAlgorithmSpec iteratedAlgorithmSpec = new IteratedSaltedPasswordAlgorithmSpec(iterationCount, salt);
    //    EncryptablePasswordSpec encryptableSpec = new EncryptablePasswordSpec(password.toCharArray(), iteratedAlgorithmSpec);
    //
    //    BCryptPassword original = null;
    //    try {
    //      original = (BCryptPassword) passwordFactory.generatePassword(encryptableSpec);
    //    } catch (InvalidKeySpecException e) {
    //      e.printStackTrace();
    //    }
    //
    //    byte[] hash = original.getHash();
    //
    //    Base64.Encoder encoder = Base64.getEncoder();
    //    BotUser botUser = new BotUser(username,encoder.encodeToString(hash),encoder.encodeToString(salt),iterationCount,role);
    botUser = new BotUser(username, password, role);
    persist(botUser);
    return botUser;
  }
}
