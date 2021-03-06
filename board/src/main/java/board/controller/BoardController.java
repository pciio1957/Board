package board.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import board.service.BoardService;
import board.vo.Board;
import board.vo.BoardSch;

@Controller
@RequestMapping("board.do") // 공통 url을 선언..
public class BoardController {
	@Autowired(required = false)
	private BoardService service;
	
	/*
	ex1) 조회화면 구현해주세요.	
	ex2) 검색되는 조회화면 구현해주세요.	
	*/	
	// http://localhost:7080/board/board.do?method=list
	// 각 기능별 메서드를 params="기능요청선언"
	@RequestMapping(params="method=list")
	public String boardList(BoardSch board, Model d) { // 모델어트리뷰트로 객체 이름으로 지원
		// service단에 요청값으로 전달해온 데이터을 board를 넣고,
		// dao에 의해 받은 결과값을 list라는 모델이름으로 사용..
		
		// 1006 BoardSch -> boardSch : 화면에 modelAttribute
		d.addAttribute("list", service.boardList( board ));
		return "a01_list";
	}
	// http://localhost:7080/board/board.do?method=insertForm
	@RequestMapping(params="method=insertForm")
	public String insertForm(Board board) {
		return "a02_Insert"; 
	}	
	// http://localhost:7080/board/board.do?method=insert	
	@RequestMapping(params="method=insert")
	public String boardInsert(Board ins) {
		service.insertBoard(ins);
		return "redirect:/board.do?method=list";
	}
	// http://localhost:7080/board/board.do?method=detail	
	@RequestMapping(params="method=detail")
	public String boardDetail(@RequestParam("no") int no, Model d) {
		System.out.println("작성글:"+service.getBoard( no ).getSubject());
		d.addAttribute("board", service.getBoard( no ));
		return "a03_detail";
	}	
	// http://localhost:7080/board/board.do?method=update	
	@RequestMapping(params="method=update")
	public String boardUpdate(Board upt) {
		service.update(upt);
		return "forward:/board.do?method=detail";
	}	
	// http://localhost:7080/board/board.do?method=delete	
	@RequestMapping(params="method=delete")
	public String boardUpdate(@RequestParam("no") int delNo) {
		service.deleteBoard(delNo);
		return "redirect:/board.do?method=list";
	}	
	// http://localhost:7080/board/board.do?method=replyFrm	
	@RequestMapping(params="method=replyFrm")
	public String boardReply(Board bd) {
		return "";
	}	
}
