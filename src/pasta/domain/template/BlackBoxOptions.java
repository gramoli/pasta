package pasta.domain.template;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import pasta.archive.ArchivableBaseEntity;
import pasta.domain.VerboseName;

@Entity
@Table(name = "black_box_options")
@VerboseName(value = "black box options", plural = "black box options")
public class BlackBoxOptions extends ArchivableBaseEntity {
	private static final long serialVersionUID = 1094750990939205404L;
	
	@Column(name = "detailed_errors")
	private boolean detailedErrors;
	
	@Column(name = "gcc_command_line_args")
	private String gccCommandLineArgs;
	
	@OneToOne
	@OnDelete(action = OnDeleteAction.CASCADE)
	@JoinColumn (name="unit_test_id")
	private UnitTest test;
	
	public BlackBoxOptions() {
		detailedErrors = true;
		gccCommandLineArgs = "-w -std=c99";
	}
	
	public BlackBoxOptions(BlackBoxOptions copy) {
		update(copy);
	}
	
	public void update(BlackBoxOptions copy) {
		if(copy == null) {
			copy = new BlackBoxOptions();
		}
		this.detailedErrors = copy.detailedErrors;
		this.gccCommandLineArgs = copy.gccCommandLineArgs;
	}
	
	public UnitTest getTest() {
		return test;
	}

	public void setTest(UnitTest test) {
		this.test = test;
	}

	public boolean isDetailedErrors() {
		return detailedErrors;
	}

	public void setDetailedErrors(boolean detailedErrors) {
		this.detailedErrors = detailedErrors;
	}

	public String getGccCommandLineArgs() {
		return gccCommandLineArgs;
	}

	public void setGccCommandLineArgs(String gccCommandLineArgs) {
		this.gccCommandLineArgs = gccCommandLineArgs;
	}
}
