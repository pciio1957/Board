package board.service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import board.dao.BoardDao;
import board.vo.Board;
import board.vo.BoardFile;

@Service
public class BoardService {
	// mybatis에서 만들어진 BoardDao를 상속받은 실제객체가 container에 메모리가 있으면
	// 할당처리..
	// 객체가 없더라도 에러가 발생하지 않게 처리..
	@Autowired(required = false)
	private BoardDao dao;
	public ArrayList<Board> boardList(Board sch){
		if(sch.getSubject()==null) sch.setSubject("");
		if(sch.getWriter()==null) sch.setWriter("");
		
		return dao.boardList(sch);
	}
	public void insertBoard(Board ins) {
		System.out.println("#첨부파일#" + ins.getReport().getOriginalFilename());
		dao.insertBoard(ins);
		uploadFile(ins.getNo(), ins.getReport());
	}
	
	// info에 저장한 값들 가져오기 
	@Value("${upload}") private String upload;
	@Value("${tmpUpload}") private String tmpUpload;
	
	public void uploadFile(int no, MultipartFile report) {
		// 1. multipartfile 객체를 file객체로 변환
		String fileName = report.getOriginalFilename();
		if(fileName != null && !fileName.equals("")) {
			File tmpFile = new File(tmpUpload + fileName);
			File orgFile = new File(upload + fileName);
			try {
				// 자동 예외처리 선언, 파일로 변환 
				report.transferTo(tmpFile);
				// 최종 웹 서버에 있는 파일 위치로 로딩
				Files.copy(tmpFile.toPath(), orgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
				
				
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// 2. DB처리하기
		dao.uploadFile(new BoardFile(fileName));
	}
	public Board getBoard(int no) {
		// 상세조회시 조회카운트 up 
		dao.uptReadCnt(no);
		return dao.getBoard(no);
	}	
	public void update(Board update) {
		dao.updateBoard(update);
	}	
	public void deleteBoard(int no) {
		dao.deleteBoard(no);
	}	
}
