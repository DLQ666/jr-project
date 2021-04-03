package com.dlq.jr.core.controller.admin;

import com.dlq.jr.core.pojo.entity.IntegralGrade;
import com.dlq.jr.core.service.IntegralGradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 *@program: jr-project
 *@description:
 *@author: Hasee
 *@create: 2021-04-03 19:43
 */
@RestController
@RequestMapping("/admin/core/integralGrade")
public class AdminIntegralGradeController {

    @Autowired
    private IntegralGradeService integralGradeService;

    @GetMapping("/list")
    public List<IntegralGrade> list(){
        return integralGradeService.list();
    }

    @DeleteMapping("/remove/{id}")
    public boolean removeById(@PathVariable("id")Long id){
        return integralGradeService.removeById(id);
    }
}
