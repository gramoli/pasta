package pasta.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "user_logins")
/**
 * @author Alex Radu
 * @version 2.0
 * @since 2013-02-25
 */
public class PASTALoginUser {
	private String username;
	private String hashedPassword;
	
	public PASTALoginUser(String username, String hashedPassword) {
		this.username = username;
		this.hashedPassword = hashedPassword;
	}
	
	public PASTALoginUser(){}
	
	@Id
	@Column(name = "username", nullable = false)
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "hashed_password", nullable = false)
	public String getHashedPassword() {
		return hashedPassword;
	}
	public void setHashedPassword(String hashedPassword) {
		this.hashedPassword = hashedPassword;
	}
	
	
}
