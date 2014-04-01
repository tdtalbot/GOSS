/*
	Copyright (c) 2014, Battelle Memorial Institute
    All rights reserved.
    Redistribution and use in source and binary forms, with or without
    modification, are permitted provided that the following conditions are met:
    1. Redistributions of source code must retain the above copyright notice, this
    list of conditions and the following disclaimer.
    2. Redistributions in binary form must reproduce the above copyright notice,
    this list of conditions and the following disclaimer in the documentation
    and/or other materials provided with the distribution.
    THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
    ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
    WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
     
    DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
    ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
    (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
    LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
    ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
    (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
    SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
    The views and conclusions contained in the software and documentation are those
    of the authors and should not be interpreted as representing official policies,
    either expressed or implied, of the FreeBSD Project.
    This material was prepared as an account of work sponsored by an
    agency of the United States Government. Neither the United States
    Government nor the United States Department of Energy, nor Battelle,
    nor any of their employees, nor any jurisdiction or organization
    that has cooperated in the development of these materials, makes
    any warranty, express or implied, or assumes any legal liability
    or responsibility for the accuracy, completeness, or usefulness or
    any information, apparatus, product, software, or process disclosed,
    or represents that its use would not infringe privately owned rights.
    Reference herein to any specific commercial product, process, or
    service by trade name, trademark, manufacturer, or otherwise does
    not necessarily constitute or imply its endorsement, recommendation,
    or favoring by the United States Government or any agency thereof,
    or Battelle Memorial Institute. The views and opinions of authors
    expressed herein do not necessarily state or reflect those of the
    United States Government or any agency thereof.
    PACIFIC NORTHWEST NATIONAL LABORATORY
    operated by BATTELLE for the UNITED STATES DEPARTMENT OF ENERGY
    under Contract DE-AC05-76RL01830
*/
package pnnl.goss.sharedperspective.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import pnnl.goss.powergrid.dao.PowergridDaoMySql;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegment;
import pnnl.goss.sharedperspective.common.datamodel.ACLineSegmentTest;
import pnnl.goss.sharedperspective.common.datamodel.ContingencyResult;
import pnnl.goss.sharedperspective.common.datamodel.ContingencyResultList;
import pnnl.goss.sharedperspective.common.datamodel.Location;
import pnnl.goss.sharedperspective.common.datamodel.Region;
import pnnl.goss.sharedperspective.common.datamodel.Substation;
import pnnl.goss.sharedperspective.common.datamodel.Topology;

public class PowergridSharedPerspectiveDaoMySql  extends PowergridDaoMySql implements PowergridSharedPerspectiveDao{

	private static Logger log = LoggerFactory.getLogger(PowergridSharedPerspectiveDaoMySql.class);

	public PowergridSharedPerspectiveDaoMySql(DataSource datasource) {
		super(datasource);
		log.debug("Creating " + PowergridSharedPerspectiveDaoMySql.class);
	}

	@Override
	public Topology getTopology(String powergridName) throws Exception {
		return getTopology(powergridName, null);
	}

	public Topology getTopology(String powergridName, String timestamp) throws Exception{
		Topology topology = new Topology();
		int powergridId = getPowergridId(powergridName);
		Region region = getRegion(powergridId);
		region.setSubstations(getSubstationList(powergridId));
		topology.setRegion(region);
		topology.setAcLineSegments(getACLineSegments(powergridId,timestamp));
		return topology;
	}

	@Override
	public Topology getTopologyUpdate(String powergridName, String timestampStr) throws Exception {
		Topology topology = new Topology();
		int powergridId = getPowergridId(powergridName);
		Region region = getRegion(powergridId);
		topology.setRegion(region);
		topology.setAcLineSegments(getACLineSegmentsUpdate(powergridId,timestampStr));
		return topology;
	}

	@Override
	public int getPowergridId(String powergridName) throws Exception {
		Connection connection = null;
		int powergridId=0;
		try{
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();
			String queryString = "select powergridid from powergrids where name = '"+powergridName+"'";
			ResultSet rs = stmt.executeQuery(queryString);
			if(rs.next()){
				powergridId = rs.getInt("powergridid");
			}

		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}
		finally{
			if(connection!=null)
				connection.close();
		}
		return powergridId;
	}

	@Override
	public Region getRegion(int powergridId) throws Exception {
		Connection connection = null;
		Region region = null;
		try{
			connection = datasource.getConnection();
			System.out.println(connection);
			Statement stmt = connection.createStatement();
			String dbQuery= "select a.mrid, a.areaName, p.name from areas a, powergrids p where a.powergridid = p.powergridid and a.powergridid = "+powergridId;
			log.debug(dbQuery);
			ResultSet rs = stmt.executeQuery(dbQuery);
			rs.next();
			region =  new Region();
			region.setName(rs.getString("areaname"));
			region.setMrid(rs.getString("mrid"));
			region.setOrganization(rs.getString("name"));
		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}
		return region;
	}

	@Override
	public List<Substation> getSubstationList(int powergridId) throws Exception{

		Connection connection= null;
		List<Substation> substations = null;

		try{

			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();
			substations = new ArrayList<Substation>();
			Substation substation = null;
			Location location =null;
			String dbQuery = "select c.mrid as rmrid , a.areaname, b.mrid, a.substationname, a.latitude, a.longitude from substations a, mridsubstations b, areas c "+
					"where a.substationid = b.substationid "+
					"and a.powergridid = b.powergridid "+
					"and a.areaname = c.areaname "+
					"and a.powergridid = "+powergridId;

			log.debug(dbQuery);
			ResultSet rs = stmt.executeQuery(dbQuery);
			while(rs.next()){
				substation = new Substation();
				substation.setMrid(rs.getString("mrid"));
				substation.setName(rs.getString("substationname"));
				location = new Location();
				location.setLatitude(rs.getDouble("latitude"));
				location.setLongitude(rs.getDouble("longitude"));
				//location.setMrid(newMrid);
				//location.setName(newName);
				substation.setLocation(location);
				substation.setRegionMRID(rs.getString("rmrid"));
				substation.setRegionName(rs.getString("areaname"));
				//substation.setRegion(region);
				substations.add(substation);
			}
			//substation.setRegion(newRegion);
		}
		catch(Exception e){
			log.error(e.getMessage());
			connection.close();
			throw e;
		}
		return substations;
	}

	@Override
	public List<ACLineSegment> getACLineSegments(int powergridId, String timestampStr) throws Exception {

		Connection connection = null;
		List<ACLineSegment> acLineSegments = null;

		try{
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();

			Timestamp timestamp;
			if(timestampStr==null){
				//Get current time -> set date to 2013-08-01 -> make sure that second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				cal.set(Calendar.MILLISECOND,  0);
				timestamp = new Timestamp(cal.getTime().getTime());
			}
			else{
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(timestampStr);
				timestamp = new Timestamp(parsedDate.getTime());
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date(timestamp.getTime()));
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				cal.set(Calendar.MILLISECOND,  0);
				timestamp = new Timestamp(cal.getTime().getTime());
			}

			String dbQuery = "select lt.timestep,l.lineid, br.branchid, mbr.mrid, bu.basekv, br.rating, br.status, lt.p, lt.q, br.frombusnumber, br.tobusnumber "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchid = mbr.branchid "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = lt.lineid "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = "+powergridId+" "+
					"and lt.timestep ='"+ timestamp+"'";


			System.out.println(dbQuery);
			ResultSet rs=stmt.executeQuery(dbQuery);
			//System.out.println(dbQuery);
			acLineSegments = new ArrayList<ACLineSegment>();
			ACLineSegment acLineSegment;

			while(rs.next()){
				String acLineName = "";
				acLineSegment = new ACLineSegment();
				acLineSegment.setMrid(rs.getString("mrid"));					//Branch's Mrid
				acLineSegment.setKvlevel(rs.getDouble("basekv")); 			//Base KV from buses 
				acLineSegment.setRating(rs.getDouble("rating")); 				//branch
				acLineSegment.setStatus(rs.getInt("status"));				//line timestep
				double mvaFlow  = Math.sqrt((rs.getDouble("p")*rs.getDouble("p"))+ (rs.getDouble("q")*rs.getDouble("q")));
				if(rs.getDouble("p")<0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow); 			//sqrt(P^2+Q^2), if P is + then positive , if Q is - then negative value.

				int fromBusNo = rs.getInt("frombusnumber");
				int toBusNo = rs.getInt("tobusnumber");
				List<Substation> substationList = new ArrayList<Substation>();
				dbQuery = "select a.mrid as rmrid, a.areaname,m.mrid, s.substationname,s.latitude,s.longitude  from buses b, substations s, mridsubstations m , areas a "+
						"where b.substationid = s.substationid "+
						"and m.substationid = b.substationid "+
						"and a.areaname = s.areaname "+
						"and b.busnumber = "+fromBusNo;
				System.out.println(dbQuery);

				Statement stmt1 = connection.createStatement();
				ResultSet rs1 = stmt1.executeQuery(dbQuery);
				if(rs1.next()){
					acLineName += rs1.getString("substationname")+"_";
					Substation substation = new Substation();
					substation.setMrid(rs1.getString("mrid"));
					substation.setName(rs1.getString("substationname"));
					//substation.setRegion(region);
					Location location = new Location();
					location.setLatitude(rs1.getDouble("latitude"));
					location.setLongitude(rs1.getDouble("longitude"));
					substation.setLocation(location);
					substation.setRegionMRID(rs1.getString("rmrid"));
					substation.setRegionName(rs1.getString("areaname"));
					substationList.add(substation);
				}

				dbQuery = "select a.mrid as rmrid, a.areaname,m.mrid, s.substationname,s.latitude,s.longitude  from buses b, substations s, mridsubstations m , areas a "+
						"where b.substationid = s.substationid "+
						"and m.substationid = b.substationid "+
						"and a.areaName = s.areaname "+
						"and b.busnumber = "+toBusNo;
				System.out.println(dbQuery);
				Statement stmt2 = connection.createStatement();
				ResultSet rs2 = stmt2.executeQuery(dbQuery);
				if(rs2.next()){
					Substation substation = new Substation();
					substation.setMrid(rs2.getString("mrid"));
					substation.setName(rs2.getString("substationname"));
					//substation.setRegion(region);
					Location location = new Location();
					location.setLatitude(rs2.getDouble("latitude"));
					location.setLongitude(rs2.getDouble("longitude"));
					substation.setLocation(location);
					substation.setRegionMRID(rs2.getString("rmrid"));
					substation.setRegionName(rs2.getString("areaname"));
					substationList.add(substation);

					acLineName += rs2.getString("substationname");
				}
				acLineSegment.setName(acLineName);		//SubstationFromName_SubstationToName from branch
				acLineSegment.setSubstations(substationList);

				acLineSegments.add(acLineSegment);
			}
		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}

		return acLineSegments;
	}

	@Override
	public List<ACLineSegment> getACLineSegmentsUpdate(int powergridId,String timestampStr) throws Exception {
		Connection connection = null;
		List<ACLineSegment> acLineSegments = null;
		try{
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();

			Calendar cal = Calendar.getInstance();
			cal.set(2013, 7, 1);
			cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
			SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
			java.util.Date parsedDate = sdf.parse(timestampStr);
			Timestamp timestamp = new Timestamp(parsedDate.getTime());

			//get current timestamp
			/*Calendar cal = Calendar.getInstance();
	    	SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a");
	    	String currentTime = sdf.format(cal.getTime()).toString();
	    	//System.out.println("Current Time = "+currentTime);
	    	sec = Integer.parseInt(currentTime.substring(6, 8));
	    	sec = sec - sec%3;
	    	String currentTimestep = currentTime.replace(currentTime.substring(6, 8), sec.toString());*/
			Calendar cal1 = Calendar.getInstance();
			cal1.setTime(new java.util.Date());
			cal1.set(2013, 7, 1);
			cal1.set(Calendar.SECOND, cal1.get(Calendar.SECOND) - cal1.get(Calendar.SECOND) % 3);
			cal1.set(Calendar.MILLISECOND,  0);
			Timestamp currentTimestamp = new Timestamp(cal1.getTime().getTime());


			//get passed timeStamp data from DB
			String dbQuery = "select lt.timestep,l.lineid, br.branchid, mbr.mrid, bu.basekv, br.rating, br.status, lt.p, lt.q, br.frombusnumber, br.tobusnumber "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchId = mbr.branchId "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = lt.lineid "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = "+powergridId+" "+
					"and lt.timestep  ='"+ timestamp+"'";
			ResultSet rs=stmt.executeQuery(dbQuery);

			dbQuery = "select lt.timestep,l.lineid, br.branchid, mbr.mrid, bu.basekv, br.rating, br.status, lt.p, lt.q, br.frombusnumber, br.tobusnumber "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchId = mbr.branchId "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = lt.lineid "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = "+powergridId+" "+
					"and lt.timestep  ='"+ currentTimestamp+"'";

			Statement stmt1 = connection.createStatement();
			ResultSet rs1=stmt1.executeQuery(dbQuery);

			acLineSegments = new ArrayList<ACLineSegment>();
			List<Substation> substationList = new ArrayList<Substation>();
			Substation substation = null;
			Location location =null;
			ACLineSegment acLineSegment;

			while(rs.next() && rs1.next()){
				double mvaFlow  = Math.sqrt((rs.getDouble("p")*rs.getDouble("p"))+ (rs.getDouble("q")*rs.getDouble("q")));
				double mvaFlow1  = Math.sqrt((rs1.getDouble("p")*rs1.getDouble("p"))+ (rs1.getDouble("q")*rs1.getDouble("q")));
				if(!(rs.getInt("status")==rs1.getInt("status") && mvaFlow==mvaFlow1))
				{
					acLineSegment = new ACLineSegment();
					String acLineName = "";
					acLineSegment.setMrid(rs1.getString("mrid"));					//Branch's Mrid
					acLineSegment.setKvlevel(rs1.getDouble("basekv")); 			//Base KV from buses 
					acLineSegment.setRating(rs1.getDouble("rating")); 				//branch
					acLineSegment.setStatus(rs1.getInt("status"));				//line timestep
					acLineSegment.setMvaFlow(mvaFlow1); 			//sqrt(P^2+Q^2), if P is + then positive , if Q is - then negative value.
					int fromBusNo = rs1.getInt("frombusnumber");
					int toBusNo = rs1.getInt("tobusnumber");
					substationList = new ArrayList<Substation>();
					dbQuery = "select a.mrid as rmrid, a.areaname,m.mrid, s.substationname,s.latitude,s.longitude  from buses b, substations s, mridsubstations m , areas a "+
							"where b.substationid = s.substationid "+
							"and m.substationid = b.substationid "+
							"and a.areaname = s.areaname "+
							"and b.busnumber = "+fromBusNo;
					//System.out.println(dbQuery);

					Statement stmt2 = connection.createStatement();
					ResultSet rs2 = stmt2.executeQuery(dbQuery);
					if(rs2.next()){
						acLineName += rs2.getString("substationname")+"_";
						substation = new Substation();
						substation.setMrid(rs2.getString("mrid"));
						substation.setName(rs2.getString("substationname"));
						//substation.setRegion(region);
						location = new Location();
						location.setLatitude(rs2.getDouble("latitude"));
						location.setLongitude(rs2.getDouble("longitude"));
						substation.setLocation(location);
						substation.setRegionMRID(rs2.getString("rmrid"));
						substation.setRegionName(rs2.getString("areaname"));
						substationList.add(substation);
					}

					dbQuery = "select a.mrid as rmrid, a.areaname,m.mrid, s.substationname,s.latitude,s.longitude  from buses b, substations s, mridsubstations m , areas a "+
							"where b.substationid = s.substationid "+
							"and m.substationid = b.substationid "+
							"and a.areaname = s.areaname "+
							"and b.busnumber = "+toBusNo;
					//System.out.println(dbQuery);
					Statement stmt3 = connection.createStatement();
					ResultSet rs3 = stmt3.executeQuery(dbQuery);
					if(rs3.next()){

						substation = new Substation();
						substation.setMrid(rs3.getString("mrid"));
						substation.setName(rs3.getString("substationname"));
						//substation.setRegion(region);
						location = new Location();
						location.setLatitude(rs3.getDouble("latitude"));
						location.setLongitude(rs3.getDouble("longitude"));
						substation.setLocation(location);
						substation.setRegionMRID(rs3.getString("rmrid"));
						substation.setRegionName(rs3.getString("areaname"));
						substationList.add(substation);
						acLineName += rs3.getString("substationname");
					}
					acLineSegment.setName(acLineName);		//SubstationFromName_SubstationToName from branch
					acLineSegment.setSubstations(substationList);

					acLineSegments.add(acLineSegment);
				}

			}


		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}
		return acLineSegments;
	}

	@Override
	public Topology getLineLoad(int powergridId, String timestamp) throws Exception {

		Connection connection = null;
		List<ACLineSegment> acLineSegments = null;
		Topology topology = new Topology();
		try{
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();


			ResultSet rs =null;

			String dbQuery="";

			Timestamp timestamp_;
			if(timestamp==null){
				//Get current time -> set date to 2013-08-01 -> make sure that second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				timestamp_ = new Timestamp(cal.getTime().getTime());
			}
			else{
				Calendar cal = Calendar.getInstance();
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(timestamp);
				timestamp_ = new Timestamp(parsedDate.getTime());
			}

			dbQuery = "select mbr.mrid, lt.p, lt.q "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchid = mbr.branchid "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = lt.lineid "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = "+powergridId+" "+
					"and lt.timestep ='"+ timestamp_+"'";

			System.out.println(dbQuery);

			rs=stmt.executeQuery(dbQuery);
			acLineSegments = new ArrayList<ACLineSegment>();
			ACLineSegment acLineSegment;
			while(rs.next()){
				acLineSegment = new ACLineSegment();
				acLineSegment.setMrid(rs.getString("mrid"));					//Branch's Mrid
				//acLineSegment.setKvlevel(rs.getDouble("basekv")); 			//Base KV from buses 
				//acLineSegment.setRating(rs.getDouble("rating")); 				//branch
				//acLineSegment.setStatus(rs.getInt("status"));				//line timestep
				double mvaFlow  = Math.sqrt((rs.getDouble("p")*rs.getDouble("p"))+ (rs.getDouble("q")*rs.getDouble("q")));
				if(rs.getDouble("p")<0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow); 			
				acLineSegments.add(acLineSegment);
			}

			topology.setAcLineSegments(acLineSegments);

		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}

		if(connection!=null)
			connection.close();

		return topology;

	}

	@Override
	public ACLineSegmentTest getLineLoadTest(int powergridId, String timestamp, int lineId) throws Exception {

		Connection connection = null;
		ACLineSegmentTest acLineSegment = null;
		ResultSet rs =null;

		try{
			connection = datasource.getConnection();
			Statement stmt = connection.createStatement();




			String dbQuery="";

			Timestamp timestamp_;
			if(timestamp==null){
				//Get current time -> set date to 2013-08-01 -> make sure that second value is multiple of 3
				Calendar cal = Calendar.getInstance();
				cal.setTime(new java.util.Date());
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				timestamp_ = new Timestamp(cal.getTime().getTime());
			}
			else{
				Calendar cal = Calendar.getInstance();
				cal.set(2013, 7, 1);
				cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) - cal.get(Calendar.SECOND) % 3);
				SimpleDateFormat sdf = new SimpleDateFormat("y-M-d H:m:s");
				java.util.Date parsedDate = sdf.parse(timestamp);
				timestamp_ = new Timestamp(parsedDate.getTime());
			}

			dbQuery = "select mbr.mrid, lt.p, lt.q,  bu.BaseKV, br.Rating, br.Status "+
					"from mridbranches mbr, branches br, buses bu, linetimesteps lt, lines_ l "+
					"where br.branchid = mbr.branchid "+
					"and br.frombusnumber = bu.busnumber "+
					"and l.lineid = "+lineId + " "+
					"and l.branchid = br.branchid "+
					"and mbr.powergridid = br.powergridid "+
					"and bu.powergridid = br.powergridid "+
					"and lt.powergridid = br.powergridid "+
					"and l.powergridid = br.powergridid "+
					"and br.powergridid = "+powergridId+" "+
					"and lt.timestep ='"+ timestamp_+"'";

			System.out.println(dbQuery);

			rs=stmt.executeQuery(dbQuery);

			if(rs.next()){
				acLineSegment = new ACLineSegmentTest();
				//acLineSegment.setMrid(rs.getString("mrid"));					//Branch's Mrid
				acLineSegment.setKvlevel(rs.getDouble("BaseKV")); 			//Base KV from buses 
				acLineSegment.setRating(rs.getDouble("Rating")); 				//branch
				acLineSegment.setStatus(rs.getInt("Status"));				//line timestep
				double mvaFlow  = Math.sqrt((rs.getDouble("p")*rs.getDouble("p"))+ (rs.getDouble("q")*rs.getDouble("q")));
				if(rs.getDouble("p")<0)
					mvaFlow = -mvaFlow;
				acLineSegment.setMvaFlow(mvaFlow); 			
			}



		}
		catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}
		finally{
			if(rs!=null)
				rs.close();
			if(connection!=null)
				connection.close();
		}

		return acLineSegment;

	}

	@Override
	public ContingencyResultList getContingencyResults(Timestamp timestamp) throws Exception {

		Connection connection = null;
		
		//The list that is going to be sent back through the DataResponse object.
		ContingencyResultList resultList = new ContingencyResultList(); 

		/* The stored procedure to return the data for a cim branch violation.  This data will return
		 * all branch violations at a specific timestep for all of the contingencies.  */
		String PROC_CTG_BR_VIO_AT_TS = "proc_GetContingencyBranchViolationsAtTimestepCim";

		//A stored procedure for getting out of service branches for a specific contingency.
		String PROC_CTG_BR = "proc_GetContingencyOutOfServiceBranchesCim";

		try{

			connection = datasource.getConnection();
			// Prepare to call the stored procedure.
			CallableStatement proc = connection.prepareCall("{ CALL " +PROC_CTG_BR_VIO_AT_TS+"(?) }");
			// Set the value of the parameter.
			proc.setTimestamp(1,  timestamp);
			// Execute the procedure on the database.
			ResultSet ctgResultSet = proc.executeQuery();			
			int contingencyId=0;
			ContingencyResult result =null;

			while(ctgResultSet.next()){
				
				if(ctgResultSet.getInt("contingencyid")!=contingencyId){
					if(result!=null){
						resultList.addResultList(result);
						result = null;
					}
					contingencyId = ctgResultSet.getInt("contingencyid");
					result = new ContingencyResult();
					result.setContingencyId(contingencyId);
					result.setContingencyName(ctgResultSet.getString("name"));
					result.setTimestamp(timestamp.toString());

					// Prepare to call the stored procedure.
					CallableStatement procBranch = connection.prepareCall("{ CALL "+PROC_CTG_BR+"(?) }");

					// Pass the contingencyId
					procBranch.setInt(1, contingencyId);

					// Execute the stored procedure.
					ResultSet brResultSet = procBranch.executeQuery();

					// Loop over branches add them to the out of service category.
					while(brResultSet.next()){
						result.addOutOfServiceACLineSegments(brResultSet.getString("mrid"));
					}
					brResultSet.close();
					brResultSet = null;
				}

				result.addViolationACLineSegments_Value(ctgResultSet.getString("mrid"),ctgResultSet.getDouble("value"));

			}

			if(result != null){
				resultList.addResultList(result);
			}
			ctgResultSet.close();

		}catch(Exception e){
			log.error(e.getMessage());
			if(connection!=null)
				connection.close();
			throw e;
		}
		finally{
			if(connection!=null)
				connection.close();
		}
		return resultList;
	}



}
