package com.wldst.ruder.module.manage.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wldst.ruder.crud.service.CrudNeo4jService;

@Service
@Transactional
public class SysCodeInfoService   {
    @Autowired
	private CrudNeo4jService cruderService;
 

     
}