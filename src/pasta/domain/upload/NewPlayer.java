package pasta.domain.upload;

import org.springframework.web.multipart.commons.CommonsMultipartFile;

public class NewPlayer {
	private CommonsMultipartFile file;

	public CommonsMultipartFile getFile() {
		return file;
	}

	public void setFile(CommonsMultipartFile file) {
		this.file = file;
	}
}
