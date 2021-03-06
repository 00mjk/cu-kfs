package edu.cornell.kfs.module.ezra.service;

import java.sql.Date;

import org.kuali.kfs.module.cg.businessobject.Agency;

import edu.cornell.kfs.module.ezra.businessobject.Sponsor;

public interface EzraService {

	public boolean updateAwardsSince(Date date);
	public boolean updateSponsorsSince(Date date);
	public boolean updateProposals();
	public Agency createAgency(Long sponsorId);
	public void updateAgency(Agency agency, Sponsor sponsor);
	
}
