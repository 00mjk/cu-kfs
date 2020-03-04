package edu.cornell.kfs.pdp.batch.fixture;

import java.text.MessageFormat;

import org.apache.commons.lang3.StringUtils;
import org.kuali.kfs.sys.KFSConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;

public enum ACHPersonPayeeFixture {
    JOHN_DOE("1234567", "jad987", "2345678", "John", "A.", "Doe", "1122333", "jad987@someplace.edu"),
    JANE_DOE("9876543", "jd8888", "8765432", "Jane", "", "Doe", "4455666", "jd8888@someplace.edu"),
    ROBERT_SMITH("5555555", "rs321", "4545454", "Robert", "", "Smith", "7766888", "rs321@anotherplace.org"),
    MARY_SMITH("9797979", "mjs22", "8866444", "Mary", "J.", "Smith", "9999999", "mjs22@anotherplace.org"),
    JACK_BROWN("1359531", "jb1000", "9999999", "Jack", "", "Brown", "1234555", "jb1000@unknowncollege.edu"),
    KFS_SYSTEM_USER("2", KFSConstants.SYSTEM_USER, "2", "KFS", "", "SYSTEMUSER", "1", "kfs-system@someplace.edu");

    private static final String FULL_NAME_FORMAT = "{0}, {1}{2}";

    public final String principalId;
    public final String principalName;
    public final String entityId;
    public final String firstName;
    public final String middleName;
    public final String lastName;
    public final String name;
    public final String employeeId;
    public final String emailAddress;

    private ACHPersonPayeeFixture(String principalId, String principalName, String entityId,
            String firstName, String middleName, String lastName, String employeeId, String emailAddress) {
        this.principalId = principalId;
        this.principalName = principalName;
        this.entityId = entityId;
        this.firstName = firstName;
        this.middleName = middleName;
        this.lastName = lastName;
        this.name = MessageFormat.format(FULL_NAME_FORMAT, lastName, firstName,
                StringUtils.isNotBlank(middleName) ? KFSConstants.BLANK_SPACE + middleName : StringUtils.EMPTY);
        this.employeeId = employeeId;
        this.emailAddress = emailAddress;
    }

    public Person toPerson() {
        return new MockACHPerson(this);
    }

    public static final class MockACHPerson extends PersonImpl {
        private static final long serialVersionUID = 1L;
        
        public MockACHPerson(ACHPersonPayeeFixture fixture) {
            super();
            this.principalId = fixture.principalId;
            this.principalName = fixture.principalName;
            this.entityId = fixture.entityId;
            this.employeeId = fixture.employeeId;
            this.emailAddress = fixture.emailAddress;
            this.name = fixture.name;
            this.active = true;
        }
    }

}
