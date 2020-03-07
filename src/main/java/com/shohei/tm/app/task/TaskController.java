/**
 * 20200118 初版
 * 
 */
package com.shohei.tm.app.task;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.shohei.tm.app.converter.LocalDateConverter;
import com.shohei.tm.domain.model.ChargeCode;
import com.shohei.tm.domain.model.Code;
import com.shohei.tm.domain.model.Project;
import com.shohei.tm.domain.model.TaskHistory;
import com.shohei.tm.domain.model.User;
import com.shohei.tm.domain.service.charge.ChargeService;
import com.shohei.tm.domain.service.code.CodeService;
import com.shohei.tm.domain.service.project.ProjectService;
import com.shohei.tm.domain.service.task.TaskService;

/**
 * タスクに関するリクエストを受けるコントローラークラス
 * バインディングのため、フォームをセットアップするメソッドとリクエストをさばくメソッドは同じクラスに含まれるように
 * してください
 * @author shoheitokumaru
 *
 */
@RequestMapping("task")
@Controller
public class TaskController {
	@Autowired
	TaskService taskService;
	@Autowired
	ProjectService projectService;
	@Autowired
	ChargeService chargeService;
	@Autowired
	CodeService codeService;
	
	//変数を指定
	//結果を格納する
	String result;

	//タスク一覧とチャージコードリストを返すビジネスロジック
	@RequestMapping(method=RequestMethod.GET)
	String listTasks(Model model) {
		//taskListsにテーブル内のタスクを取得
		List<TaskHistory> taskLists = taskService.findAllTaskLists();
		//プロジェクトIDリストを取得
		List<Project> projectLists = projectService.findAll();
		//コードマスタ
		List<Code> codeList = codeService.findAll();
		
		//取得した値をmodelに追加する
		model.addAttribute("task_lists", taskLists);
		model.addAttribute("project_lists", projectLists);
		model.addAttribute("codeList", codeList);
		return "task/task-list";
	}

	/**
	 * タスク一覧画面→タスク詳細クリック→タスク詳細を編集する画面へ遷移
	 * 渡す値：task_historyのカラム
	 * code, chargeCode.code, name, detail, content, plan, 
	 * @param model
	 * @return
	 */
	@RequestMapping(path="{id}", method=RequestMethod.GET)
	String goToTaskEdit(@PathVariable("id") Integer id,
			Model model) {
		//パスに入力されたidのデータを取得
		TaskHistory taskDetailData = taskService.findOne(id);
		List<Code> codeList = codeService.findAll();
					
		//取得した値をModelに格納
		model.addAttribute("task_detail_data", taskDetailData);
		model.addAttribute("codeList", codeList);
		model.addAttribute("year", getDevidedDate("year", taskDetailData.getDeadlineDate()));
		model.addAttribute("month", getDevidedDate("month", taskDetailData.getDeadlineDate()));
		model.addAttribute("day", getDevidedDate("day", taskDetailData.getDeadlineDate()));
		
		return "task/task-edit";
	}

	/**
	 * タスク詳細編集画面→データ更新→タスク詳細編集画面
	 * 渡す値：task_history.id
	 * @param model
	 * @return
	 */
	@RequestMapping(path="edit/{id}", method=RequestMethod.POST)
	String updateTaskDetail(
			//フォームで入力された情報を保持
			@Validated TaskDetailForm form, 
			//@PathVariable リクエストパラメータ"roomId"に入っている値を取得する
			@PathVariable("id") Integer id, 
			Model model			
			) {
		
		//フォームの入力値を取得
		String status = form.getStatus();
		String progressRate = form.getProgressRate();
		String content = form.getContent();
		String problem = form.getProblem();
		String plan = form.getPlan();
		LocalDate deadlineDate = createDeadlineDate(form.getYear(), form.getMonth(), form.getDay());

				
		//データを更新
		taskService.updateTaskDetailById(status, progressRate, deadlineDate, content, problem, plan, id);
		//return "redirect:/task/{id}";
		
		return this.goToTaskEdit(id, model);
	}

	@RequestMapping(path="task-add-info", method=RequestMethod.GET)
	String gotoTaskAddInfo(Model model) {
		//カレントのステータスを取得
		List<Project> currentProjectList = projectService.findAll();
		List<ChargeCode> currentChargeList = chargeService.findAll();
		List<TaskHistory> currentTaskList = taskService.findAllTaskLists();
		List<Code> codeList = codeService.findAll();
		
		//取得した値をmodelに突っ込む
		model.addAttribute("currentProjectList", currentProjectList);
		model.addAttribute("currentChargeList", currentChargeList);		
		model.addAttribute("currentTaskList", currentTaskList);		
		model.addAttribute("codeList", codeList);
		model.addAttribute("year", getCurrentDate("year"));
		model.addAttribute("month", getCurrentDate("month"));
		model.addAttribute("day", getCurrentDate("day"));
		
		return "task/task-add-info";
	}

	/**
	 * タスク情報を削除する
	 * 渡す値：id
	 * @param model
	 * @return
	 */
	@RequestMapping(path="task-add-info/delete/{id}", method=RequestMethod.POST)
	String deleteTaskInfo(
			//@PathVariable リクエストパラメータ"roomId"に入っている値を取得する
			@PathVariable("id") Integer id, 
			Model model			
			) {
				
		//データを削除
		taskService.deleteTaskInfoById(id); 
		return this.gotoTaskAddInfo(model);
	}

	
	/**
	 * タスク情報を追加する
	 * @param form
	 * @param bindingResult
	 * @param model
	 * @return
	 */
	@RequestMapping(method=RequestMethod.POST)
	String addTaskInfo(
			//フォームで入力された情報を保持
			@Validated TaskForm form,
			//フォームクラスの入力チェック結果を保持
			BindingResult bindingResult,
			Model model) {
		
		//年月日を取得
		String year = form.getYear();
		String month = form.getMonth();
		String day = form.getDay();
		
		//年月日を解析
//		if(!checkDate(year, month, day)) {
//			model.addAttribute("date_error_flg", true);
//			model.addAttribute("date_error_msg", "えらーだっちゃ");
//		} 
		
		//その他入力値チェック
		if (bindingResult.hasErrors()) {
			return gotoTaskAddInfo(model);
		}

		//取得する値を定義
		Project projectId; //プロジェクトコードのPK
		String projectCodeId;
		ChargeCode chargeId;  //チャージコードのPK
		String code; //SES_100
		String detail; //タスク名
		LocalDate deadlineDate; //期日
		String status; //ステータス
		String progressRate; //進捗度		
		
		//formの値を取得
		projectId = form.getProjectId();
		projectCodeId = form.getProjectCodeId();
		chargeId = form.getChargeId();
		//code = form.getCode();
		detail = form.getDetail();
		//deadlineDate = form.getDeadlineDate();
		status = form.getStatus();
		progressRate = form.getProgressRate();
		
		//codeを生成
		code = projectId.getCode() + "_" + projectCodeId;

		//ダミーのUserを定義
		User user = new User();
		user.setId(1);
		
		//deadlineDateを生成
		deadlineDate = createDeadlineDate(year, month, day);

		//formの値をTaskHistoryに詰める
		TaskHistory taskHistory = new TaskHistory();
		taskHistory.setUser(user);
		taskHistory.setProjectId(projectId);
		taskHistory.setChargeCode(chargeId);
		taskHistory.setCode(code);
		taskHistory.setDetail(detail);
		taskHistory.setDeadlineDate(deadlineDate);
		taskHistory.setStatus(status);	
		taskHistory.setProgressRate(progressRate);
		
		//TaskHistoryに値を追加
		taskService.save(taskHistory);
		
		return "redirect:/task/task-add-info";
	}

	//リクエストパラメータをバインドする
	@ModelAttribute
	TaskForm setUpTaskForm() {
		TaskForm form = new TaskForm();
		return form;
	}
	
	//リクエストパラメータをバインドする
	@ModelAttribute
	TaskDetailForm setUpTaskDetailForm() {
		TaskDetailForm form = new TaskDetailForm();
		return form;
	}
	
	//LocalDate(yyyy-mm-dd)を文字列の年月日に分解して返す
	private String getDevidedDate(String flg, LocalDate date) {
		if(flg.equals("year")) {
			return String.valueOf(date.getYear());
		} else if (flg.equals("month")) {
			return String.valueOf(date.getMonthValue());
		} else {
			return String.valueOf(date.getDayOfMonth());
		}
	}

	//カレントの年月日を返すメソッド
	private String getCurrentDate(String flg) {
		Calendar cal = Calendar.getInstance();
		if (flg.equals("year")) {			
			String v = String.valueOf(cal.get(Calendar.YEAR));
			if (v.length() == 1) {//ありえない!
				v = "0" + v;
			}
			return v;
		} else if (flg.equals("month")) {
			String v = String.valueOf(cal.get(Calendar.MONTH) + 1);
			if (v.length() == 1) {
				v = "0" + v;
			}
			return v;			
		} else {
			String v = String.valueOf(cal.get(Calendar.DATE));
			if (v.length() == 1) {
				v = "0" + v;
			}
			return v;			
		}
	}
	
	//DeadlineDateを生成するメソッド
	private LocalDate createDeadlineDate(String year, String month, String day) {
		LocalDateConverter conv = new LocalDateConverter();
		return conv.convertToLocalDate(year, month, day, "yyyyMMdd");
	}
	
	//入力された年月日が存在するかチェックするメソッド
	private boolean checkDate(String year, String month, String day) {
	    try {
	        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
	        String s1 = year + "/" + month + "/" + day;
	        dtf.format(LocalDate.parse(s1, dtf));
	        return true;
	    } catch (DateTimeParseException dtp) {
	        return false;
	    }
	}
}
