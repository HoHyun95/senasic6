package senasic.web.controllers;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.Gson;

import senasic.web.DAO.PetBoardDAO;
import senasic.web.DTO.PetBoardDTO;
import senasic.web.DTO.PetBoard_RelpyDTO;
import senasic.web.DTO.petBoard_recDTO;
import statics.Statics;

@WebServlet("*.pet")
public class PetBoardController extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		request.setCharacterEncoding("utf8");

		String url = request.getRequestURI();
		String ctxPath = request.getContextPath();
		String cmd = url.substring(ctxPath.length());
		Gson g = new Gson();

		System.out.println(cmd);

		PetBoardDAO dao = PetBoardDAO.getInstance();

		try {

			// 게시판 리스트 출력
			if (cmd.equals("/list.pet")) {
				List<PetBoardDTO> list;

				int currentPage = Integer.parseInt(request.getParameter("cpage"));
				int pageTotalCount;

				pageTotalCount = dao.getPageTotalCount();

				if (currentPage < 1) {
					currentPage = 1;
				}
				if (currentPage > pageTotalCount) {
					currentPage = pageTotalCount;
				}

				int start = currentPage * Statics.RECORD_COUNT_PER_PAGE - (Statics.RECORD_COUNT_PER_PAGE - 1);
				int end = currentPage * Statics.RECORD_COUNT_PER_PAGE;

				list = dao.selectByBound(start, end);
				String navi = dao.getPageNavi(currentPage);
				request.setAttribute("cpage", currentPage);
				request.setAttribute("list", list);
				request.setAttribute("navi", navi);
				request.getRequestDispatcher("board/boardList.jsp").forward(request, response);

				// 카테고리
			} else if (cmd.equals("/category.pet")) {

				List<PetBoardDTO> list;

				int currentPage = Integer.parseInt(request.getParameter("cpage"));
				String category = request.getParameter("category");
				int pageTotalCount;

				System.out.println(currentPage);
				System.out.println(category);

				pageTotalCount = dao.getPageTotalCountByCategory(category);

				if (currentPage < 1) {
					currentPage = 1;
				}
				if (currentPage > pageTotalCount) {
					currentPage = pageTotalCount;
				}

				int start = currentPage * Statics.RECORD_COUNT_PER_PAGE - (Statics.RECORD_COUNT_PER_PAGE - 1);
				int end = currentPage * Statics.RECORD_COUNT_PER_PAGE;

				list = dao.selectByCategory(category, start, end);
				String navi = dao.getPageNaviByCategory(currentPage, category);

				request.setAttribute("cpage", currentPage);
				request.setAttribute("list", list);
				request.setAttribute("navi", navi);
				request.getRequestDispatcher("board/boardList.jsp").forward(request, response);

				// 게시판 글쓰기로 이동
			} else if (cmd.equals("/write.pet")) {
				response.sendRedirect("board/boardWrite.jsp");

				// 게시판 글 작성 완료
			} else if (cmd.equals("/input.pet")) {
				String category = request.getParameter("category");
				String writer = (String) request.getSession().getAttribute("loginID");
				String title = request.getParameter("title");
				String contents = request.getParameter("contents");
				
				System.out.println(category);
				System.out.println(writer);
				System.out.println(title);
				System.out.println(contents);
				

				int result = dao.insert(category, writer, title, contents);
				request.setAttribute("result", result);
				request.getRequestDispatcher("board/boardWrite.jsp").forward(request, response);

				// 게시판 상세보기
			} else if (cmd.equals("/detail.pet")) {
				int seq = Integer.parseInt(request.getParameter("seq"));
				int cpage = Integer.parseInt(request.getParameter("cpage"));
				String longinID = (String) request.getSession().getAttribute("loginID");


				int recCheck = dao.recCheck(seq, longinID); // 추천 여부
				int user = 0;
				if (recCheck == 1) {
					user = 0;
				} else if (recCheck == 0) {
					user = 1;
				}

				List<PetBoardDTO> list = dao.information(seq);
				List<PetBoard_RelpyDTO> replyList = dao.selectAllReply(seq);
				int CountComment = dao.getCountComment(seq);

				int result = dao.addViewCount(seq);

				request.setAttribute("CountComment", CountComment);
				request.setAttribute("cpage", cpage);
				request.setAttribute("list", list);
				request.setAttribute("replyList", replyList);
				request.setAttribute("user", user);
				request.getRequestDispatcher("board/boardDetail.jsp").forward(request, response);

				// 댓글 달기
			} else if (cmd.equals("/comment.pet")) {

				String writer = (String) request.getSession().getAttribute("loginID");
				String comment = request.getParameter("comment");
				int board_seq = Integer.parseInt(request.getParameter("seq"));
				int cpage = Integer.parseInt(request.getParameter("cpage"));

				int reply = dao.insertReply(board_seq, writer, comment);
				dao.updateComment(board_seq);

				response.sendRedirect("/detail.pet?seq=" + board_seq + "&cpage=" + cpage);

				// 댓글 삭제 기능
			} else if (cmd.equals("/deleteComment.pet")) {

				int board_seq = Integer.parseInt(request.getParameter("board_seq"));
				int seq = Integer.parseInt(request.getParameter("seq"));
				int cpage = Integer.parseInt(request.getParameter("cpage"));

				System.out.println(seq);
				System.out.println(cpage);

				int deleteComment = dao.deleteComment(seq);
				dao.updateComment(board_seq);

				response.sendRedirect("/detail.pet?seq=" + board_seq + "&cpage=" + cpage);

				// 게시판 삭제기능
			} else if (cmd.equals("/delete.pet")) {
				int seq = Integer.parseInt(request.getParameter("seq"));

				int result = dao.delete(seq);
				response.sendRedirect("/list.pet?cpage=1");

				// 게시판 수정창으로 이동
			} else if (cmd.equals("/modify.pet")) {

				int seq = Integer.parseInt(request.getParameter("seq"));
				int cpage = Integer.parseInt(request.getParameter("cpage"));

				List<PetBoardDTO> list = dao.information(seq);

				request.setAttribute("list", list);
				request.setAttribute("cpage", cpage);
				request.getRequestDispatcher("board/modify_boardWrite.jsp").forward(request, response);

				
				// 게시판 수정하기
			} else if (cmd.equals("/modify_board.pet")) {
				String category = request.getParameter("category");
				String title = request.getParameter("title");
				String contents = request.getParameter("contents");
				int seq = Integer.parseInt(request.getParameter("seq"));
				int cpage = Integer.parseInt(request.getParameter("cpage"));
				String longinID = (String) request.getSession().getAttribute("loginID");
				
				System.out.println(category);
				System.out.println(title);
				System.out.println(contents);
				System.out.println(seq);
				System.out.println(cpage);
				System.out.println(longinID);
				
				
				int result = dao.Modify(category, title, contents, seq);
				
				int recCheck = dao.recCheck(seq, longinID); // 추천 여부
				int user = 0;
				if (recCheck == 1) {
					user = 0;
				} else if (recCheck == 0) {
					user = 1;
				}

				List<PetBoardDTO> list = dao.information(seq);
				List<PetBoard_RelpyDTO> replyList = dao.selectAllReply(seq);
				int CountComment = dao.getCountComment(seq);

				dao.addViewCount(seq);

				request.setAttribute("CountComment", CountComment);
				request.setAttribute("cpage", cpage);
				request.setAttribute("list", list);
				request.setAttribute("replyList", replyList);
				request.setAttribute("user", user);
				request.setAttribute("result", result);
				request.getRequestDispatcher("board/boardDetail.jsp").forward(request, response);
				
				
				
				// 게시판 검색기능
			} else if (cmd.equals("/search.pet")) {

				String keyword = request.getParameter("keyword");
				String searchWord = request.getParameter("searchWord");
				if (keyword.equals("제목")) {
					keyword = "title";
				} else if (keyword.equals("작성자")) {
					keyword = "writer";
				}

				List<PetBoardDTO> list;
				int currentPage = Integer.parseInt(request.getParameter("cpage"));

				int pageTotalCount;

				pageTotalCount = dao.getPageTotalCountBySearch(keyword, searchWord);

				if (currentPage < 1) {
					currentPage = 1;
				}
				if (currentPage > pageTotalCount) {
					currentPage = pageTotalCount;
				}

				int start = currentPage * Statics.RECORD_COUNT_PER_PAGE - (Statics.RECORD_COUNT_PER_PAGE - 1);
				int end = currentPage * Statics.RECORD_COUNT_PER_PAGE;

				list = dao.search(keyword, searchWord, start, end);
				String navi = dao.getPageNaviBySearch(pageTotalCount, keyword, searchWord);
				request.setAttribute("cpage", currentPage);
				request.setAttribute("list", list);
				request.setAttribute("navi", navi);
				request.getRequestDispatcher("board/boardList.jsp").forward(request, response);

				// 추천 기능
			} else if (cmd.equals("/likes.pet")) {

				int rec_seq = Integer.parseInt(request.getParameter("seq"));
				String rec_id = (String) request.getSession().getAttribute("loginID");

				int result = dao.recCheck(rec_seq, rec_id);
				int user = 0;
				if (result == 1) {
					dao.recDelete(rec_seq, rec_id);
					user = 0;
				} else if (result == 0) {
					dao.recInsert(new petBoard_recDTO(rec_seq, rec_id));
					user = 1;
				}

				int a = dao.recUpdate(rec_seq);
				int num = dao.getRecNum(rec_seq);

				int[] arr = new int[2];
				arr[0] = num; // 추천수
				arr[1] = user; // 추천 유무

				String answer = g.toJson(arr);
				response.getWriter().append(answer);

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

}
