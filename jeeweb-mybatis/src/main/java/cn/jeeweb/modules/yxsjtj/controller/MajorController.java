package cn.jeeweb.modules.yxsjtj.controller;

import cn.jeeweb.core.common.data.DuplicateValid;
import cn.jeeweb.core.model.AjaxJson;
import cn.jeeweb.core.model.PageJson;
import cn.jeeweb.core.model.ValidJson;
import cn.jeeweb.core.query.annotation.PageableDefaults;
import cn.jeeweb.core.query.data.PropertyPreFilterable;
import cn.jeeweb.core.query.data.Queryable;
import cn.jeeweb.core.query.utils.QueryableConvertUtils;
import cn.jeeweb.core.query.wrapper.EntityWrapper;
import cn.jeeweb.core.security.shiro.authz.annotation.RequiresMethodPermissions;
import cn.jeeweb.core.utils.ObjectUtils;
import cn.jeeweb.core.utils.StringUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import cn.jeeweb.core.common.controller.BaseBeanController;
import cn.jeeweb.core.security.shiro.authz.annotation.RequiresPathPermission;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.jeeweb.modules.sys.entity.Attachment;
import cn.jeeweb.modules.yxsjtj.entity.Major;
import cn.jeeweb.modules.yxsjtj.service.IMajorService;

/**   
 * @Title: 专业
 * @Description: 专业
 * @author xiaoming
 * @date 2017-12-07 22:23:04
 * @version V1.0   
 *
 */
@Controller
@RequestMapping("${admin.url.prefix}/yxsjtj/major")
@RequiresPathPermission("yxsjtj:major")
public class MajorController extends BaseBeanController<Major> {

    @Autowired
    protected IMajorService majorService;

    public Major get(String id) {
        if (!ObjectUtils.isNullOrEmpty(id)) {
            return majorService.selectById(id);
        } else {
            return newModel();
        }
    }
    
    /**
  	 * 
  	 * @title: ajaxUpload
  	 * @description: 文件上传
  	 * @param request
  	 * @param response
  	 * @param files
  	 * @return
  	 * @return: AjaxUploadResponse
  	 */
  	@RequestMapping(value = "uploadSimditor", method = RequestMethod.POST)
  	@ResponseBody
  	public AjaxJson uploadSimditor(HttpServletRequest request, HttpServletResponse response) {
  		response.setContentType("text/plain");
  		AjaxJson ajaxJson = new AjaxJson();
  		List<Attachment> attachmentList = new ArrayList<Attachment>();
  		
  		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
  				request.getSession().getServletContext());
  		Map<String, Object> data = new HashMap<String, Object>();
  		boolean isSuccess = false;
  		if (multipartResolver.isMultipart(request)) { // 判断request是否有文件上传
  			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
  			Iterator<String> ite = multiRequest.getFileNames();
  			while (ite.hasNext()) {
  				MultipartFile file = multiRequest.getFile(ite.next());
  				try {
  					isSuccess = majorService.resolverAttch(request, file);
  				} catch (Exception e) {
  					isSuccess = false;
  					e.printStackTrace();
  				}
  				break;
  			}
  		} 
  		Attachment attachment = new Attachment();
  		attachment.setStatus(isSuccess+"");
  		attachment.setId("id");
  		attachmentList.add(attachment);
  		ajaxJson.setData(attachmentList);
  		System.out.println("uploadSimditor:"+isSuccess);
  		return ajaxJson;
  	}
      

    @RequiresMethodPermissions("list")
    @RequestMapping(method = RequestMethod.GET)
    public String list(Model model, HttpServletRequest request, HttpServletResponse response) {
        return display("list");
    }

    @RequestMapping(value = "ajaxList", method = { RequestMethod.GET, RequestMethod.POST })
    @PageableDefaults(sort = "id=desc")
    private void ajaxList(Queryable queryable, PropertyPreFilterable propertyPreFilterable, HttpServletRequest request,
                          HttpServletResponse response) throws IOException {
        EntityWrapper<Major> entityWrapper = new EntityWrapper<Major>(entityClass);
        propertyPreFilterable.addQueryProperty("id");
        // 预处理
        QueryableConvertUtils.convertQueryValueToEntityValue(queryable, entityClass);
        SerializeFilter filter = propertyPreFilterable.constructFilter(entityClass);
        PageJson<Major> pagejson = new PageJson<Major>(majorService.list(queryable,entityWrapper));
        String content = JSON.toJSONString(pagejson, filter);
        StringUtils.printJson(response, content);
    }

    @RequestMapping(value = "create", method = RequestMethod.GET)
    public String create(Model model, HttpServletRequest request, HttpServletResponse response) {
        if (!model.containsAttribute("data")) {
            model.addAttribute("data", newModel());
        }
        return display("edit");
    }

    @RequestMapping(value = "create", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJson create(Model model, @Valid @ModelAttribute("data") Major major, BindingResult result,
                           HttpServletRequest request, HttpServletResponse response) {
        return doSave(major, request, response, result);
    }

    @RequestMapping(value = "{id}/update", method = RequestMethod.GET)
    public String update(@PathVariable("id") String id, Model model, HttpServletRequest request,
                              HttpServletResponse response) {
        Major major = get(id);
        model.addAttribute("data", major);
        return display("edit");
    }

    @RequestMapping(value = "{id}/update", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJson update(Model model, @Valid @ModelAttribute("data") Major major, BindingResult result,
                           HttpServletRequest request, HttpServletResponse response) {
        return doSave(major, request, response, result);
    }

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJson doSave(Major major, HttpServletRequest request, HttpServletResponse response,
                           BindingResult result) {
        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.success("保存成功");
        if (hasError(major, result)) {
            // 错误提示
            String errorMsg = errorMsg(result);
            if (!StringUtils.isEmpty(errorMsg)) {
                ajaxJson.fail(errorMsg);
            } else {
                ajaxJson.fail("保存失败");
            }
            return ajaxJson;
        }
        try {
            if (StringUtils.isEmpty(major.getId())) {
                majorService.insert(major);
            } else {
                majorService.insertOrUpdate(major);
            }
        } catch (Exception e) {
            e.printStackTrace();
            ajaxJson.fail("保存失败!<br />原因:" + e.getMessage());
        }
        return ajaxJson;
    }

    @RequestMapping(value = "{id}/delete", method = RequestMethod.POST)
    @ResponseBody
    public AjaxJson delete(@PathVariable("id") String id) {
        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.success("删除成功");
        try {
            majorService.deleteById(id);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxJson.fail("删除失败");
        }
        return ajaxJson;
    }

    @RequestMapping(value = "batch/delete", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public AjaxJson batchDelete(@RequestParam(value = "ids", required = false) String[] ids) {
        AjaxJson ajaxJson = new AjaxJson();
        ajaxJson.success("删除成功");
        try {
            List<String> idList = java.util.Arrays.asList(ids);
            majorService.deleteBatchIds(idList);
        } catch (Exception e) {
            e.printStackTrace();
            ajaxJson.fail("删除失败");
        }
        return ajaxJson;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String view(Model model, @PathVariable("id") String id, HttpServletRequest request,
                       HttpServletResponse response) {
        Major major = get(id);
        model.addAttribute("data", major);
        return display("edit");
    }

    @RequestMapping(value = "validate", method = { RequestMethod.GET, RequestMethod.POST })
    @ResponseBody
    public ValidJson validate(DuplicateValid duplicateValid, HttpServletRequest request) {
        ValidJson validJson = new ValidJson();
        Boolean valid = Boolean.FALSE;
        try {
            EntityWrapper<Major> entityWrapper = new EntityWrapper<Major>(entityClass);
            valid = majorService.doValid(duplicateValid,entityWrapper);
            if (valid) {
                validJson.setStatus("y");
                validJson.setInfo("验证通过!");
            } else {
                validJson.setStatus("n");
                if (!StringUtils.isEmpty(duplicateValid.getErrorMsg())) {
                    validJson.setInfo(duplicateValid.getErrorMsg());
                } else {
                    validJson.setInfo("当前信息重复!");
                }
            }
        } catch (Exception e) {
            validJson.setStatus("n");
            validJson.setInfo("验证异常，请检查字段是否正确!");
        }
        return validJson;
    }
}