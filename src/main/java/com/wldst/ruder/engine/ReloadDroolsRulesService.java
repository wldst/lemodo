package com.wldst.ruder.engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieContainer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wldst.ruder.crud.service.CrudNeo4jService;
import com.wldst.ruder.exception.DefineException;
import com.wldst.ruder.module.fun.Neo4jOptCypher;
import com.wldst.ruder.util.CrudUtil;
import com.wldst.ruder.util.MapTool;

@Service
public class ReloadDroolsRulesService {
//    @Autowired
    public static KieContainer kieContainer;
    @Autowired
	private CrudUtil crudUtil;
    @Autowired
	private CrudNeo4jService neo4jService;
	
    public  void reload(){
        KieContainer kieContainer=loadContainerFromString(loadRules());
        this.kieContainer=kieContainer;
    }
    
    public List<Map<String, Object>>  loadRules(){
    	return loadRules(null);
    }

    public List<Map<String, Object>>  loadRules(Map<String, Object> body){
		if(body==null) {
			body = new HashMap<>(); // 请求body
		}
		String label = "Rule";
		String[] columns;
		try {
			columns = crudUtil.getMdColumns(label);
			String query = Neo4jOptCypher.safeQueryObj(body, label, columns);
			return neo4jService.query(query,body);
		} catch (DefineException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
    }
    

    private  KieContainer loadContainerFromString(List<Map<String, Object>> rules) {
    	
        long startTime = System.currentTimeMillis();
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();
		if (rules != null && !rules.isEmpty()) {
			for (Map<String, Object> rule : rules) {
				String drl = MapTool.string(rule, "content");
				kfs.write("src/main/resources/" + drl.hashCode() + ".drl", drl);
			}
		}

        KieBuilder kb = ks.newKieBuilder(kfs);

        kb.buildAll();
        if (kb.getResults().hasMessages(Message.Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time to build rules : " + (endTime - startTime)  + " ms" );
        startTime = System.currentTimeMillis();
        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        endTime = System.currentTimeMillis();
        System.out.println("Time to load container: " + (endTime - startTime)  + " ms" );
        return kContainer;
    }
    
}
