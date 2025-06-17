package com.wldst.ruder.module.database;

public class DatabaseMetaInterfaceFactory
{

	/** @Fields REGISTER_IMPLEMENTS_CLASS  */
	private static final Class<?> [] REGISTER_IMPLEMENTS_CLASS = new Class[]{
				MySQLDataBaseMeta.class, 
				PostgreSQLDatabaseMeta.class,
				GBaseDataBaseMeta.class,
				InformixDataBaseMeta.class,
				OracleDatabaseMeta.class
	};		

	
	/** @Fields dbMetaInterfaces  */
	private static DatabaseMetaInterface [] dbMetaInterfaces = null;
	/** @Fields isInit  */
	private static boolean isInit = false;
	
	/**
	 * @return  
	 * DatabaseMetaInterface[]
	 */
	public static synchronized DatabaseMetaInterface[] initAllImplementClass(){
		if(!isInit){
			if(dbMetaInterfaces!=null) return dbMetaInterfaces;			
			dbMetaInterfaces = new DatabaseMetaInterface[REGISTER_IMPLEMENTS_CLASS.length];			
			int i=0;			
			for(Class c :REGISTER_IMPLEMENTS_CLASS){			
				try {
					Class.forName(REGISTER_IMPLEMENTS_CLASS[i].getName());
					dbMetaInterfaces[i] = (DatabaseMetaInterface) c.newInstance();
				} catch (Exception e) {
					throw new RuntimeException("encountered exception while get instantce from All implements :",e);
				} 
				i++;				
			}		
			isInit = true;
		}
		return dbMetaInterfaces;
	}
	
	
	/**
	 * @param type 
	 * @return  
	 * DatabaseMetaInterface
	 */
	public static DatabaseMetaInterface  getInstance(String type){
		
		DatabaseMetaInterface retVal = null;
		try{
			for(DatabaseMetaInterface dbMeta: initAllImplementClass()){
				if(dbMeta.getDatabaseTypeDesc().toLowerCase().equalsIgnoreCase(type.toLowerCase())|| dbMeta.getDatabaseTypeDescLong().equalsIgnoreCase(type.toLowerCase())){
					retVal = (DatabaseMetaInterface) dbMeta.clone();
				}
			}
		}catch(Exception e){
			throw new RuntimeException(e);
		}
		return retVal;
		
		
	}	
}