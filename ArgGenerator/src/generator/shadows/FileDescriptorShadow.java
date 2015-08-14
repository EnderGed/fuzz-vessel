package generator.shadows;

/*
 * There is no point creating a file descriptor on the Vessel side.
 * What we want is to pass a file name to the Ghost and let is get
 * a related file descriptor by itself.
 */
public class FileDescriptorShadow {
	private String fileName;

	public FileDescriptorShadow(String fn) {
		fileName = fn;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
