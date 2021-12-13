package senasic.web.controllers;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.oreilly.servlet.MultipartRequest;
import com.oreilly.servlet.multipart.DefaultFileRenamePolicy;

import senasic.web.DAO.AdminDAO;
import senasic.web.DAO.RestBoardDAO;
import senasic.web.DTO.MemberDTO;
import senasic.web.DTO.MenuDTO;
import senasic.web.DTO.RestBoardDTO;
import statics.Statics;
import utils.passwordUtils;


@WebServlet("*.admin")
public class AdminController extends HttpServlet {
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String uri = request.getRequestURI();
		String ctx = request.getContextPath();
		String cmd = uri.substring(ctx.length()+1);
		AdminDAO dao = AdminDAO.getInstance();
		RestBoardDAO rbdao = RestBoardDAO.getInstance();
		try {
			if(cmd.equals("rbUpload.admin")) { //식당게시판 글 업로드
				int maxSize = 1024*1024*10; //10m
				//savepath 경로 변경
				String savePath = "C:\\Users\\limdo\\git\\senasic6\\src\\main\\webapp\\Restaurant/RestImg";
				File filePath = new File(savePath);
				if(!filePath.exists()) {filePath.mkdir();}
				System.out.println(savePath);
				MultipartRequest multi = new MultipartRequest(request,savePath,maxSize,"UTF8",new DefaultFileRenamePolicy());

				multi.getParameter(savePath);

				String oriName1 = multi.getOriginalFileName("photo[0]");
				String sysName1 = multi.getFilesystemName("photo[0]");
				String oriName2 = multi.getOriginalFileName("photo[1]");
				String sysName2 = multi.getFilesystemName("photo[1]");
				String oriName3 = multi.getOriginalFileName("photo[2]");
				String sysName3 = multi.getFilesystemName("photo[2]");

				String title = multi.getParameter("title");
				String ctg = multi.getParameter("category");
				String loc = multi.getParameter("location");
				String loc_detail = multi.getParameter("location_detail");
				String phone = multi.getParameter("phone");
				String hour = multi.getParameter("hour");
				String garage = multi.getParameter("garage");
				String link = multi.getParameter("link");
				String m1 = multi.getParameter("menu1");
				int p1 = Integer.parseInt(multi.getParameter("price1"));
				String m2 = multi.getParameter("menu2");
				int p2 = Integer.parseInt(multi.getParameter("price2"));
				String m3 = multi.getParameter("menu3");
				int p3 = Integer.parseInt(multi.getParameter("price3"));
				String root = "\\Restaurant\\RestImg\\";
				int result = dao.writeRest(title, loc, loc_detail, ctg, hour, garage, phone, root+sysName1, root+sysName2, root+sysName3, link);
				int menu = dao.insertMenu(title, m1, p1, m2, p2, m3, p3);
				response.sendRedirect("/admin/restBoardWrite.jsp");
				
			}else if(cmd.equals("rbEdit.admin")){ //식당게시판 글목록
				int currentPage=1;
				if(request.getParameter("cpage")!=null) {currentPage = Integer.parseInt(request.getParameter("cpage"));}
	            int pageTotalCount = rbdao.getPageTotalCount();
	            if(currentPage <1) {currentPage = 1;}
	            if(currentPage > pageTotalCount) {currentPage = pageTotalCount;}
	            int start = currentPage * Statics.ADMIN_COUNT_PER_PAGE - (Statics.ADMIN_COUNT_PER_PAGE-1);
	            int end = currentPage * Statics.ADMIN_COUNT_PER_PAGE;
	            List<RestBoardDTO> list = rbdao.selectByList(start, end);

	             List<Integer> navi = dao.getPageNavi(currentPage);
	             request.setAttribute("start", start-1);
	             request.setAttribute("end", end+1);
	             request.setAttribute("navi", navi);
	             request.setAttribute("cpage", currentPage);
				request.setAttribute("list", list);
	             request.getRequestDispatcher("/admin/restBoardEdit.jsp").forward(request, response);
	             
			}else if(cmd.equals("rbWrite.admin")) { //식당게시판 글쓰기 접근,전체 접근권한 예정
				//수정에정
				if(request.getSession().getAttribute("loginID")!=null) {
					String id = request.getSession().getAttribute("loginID").toString();					
				}
				request.getRequestDispatcher("/admin/restBoardWrite.jsp").forward(request, response);
				
			}else if(cmd.equals("rbDetail.admin")){ //식당게시판 상세정보 수정접근
				int num = Integer.parseInt(request.getParameter("num"));
				RestBoardDTO dto = dao.getRestBoardInfo(num);
				MenuDTO Mdto = dao.getMenuInfo(dto.getTitle());
				request.setAttribute("dto", dto);
				request.setAttribute("Mdto", Mdto);
				request.getRequestDispatcher("/admin/restBoardEditDetail.jsp").forward(request, response);
				
			}else if(cmd.equals("rbModify.admin")) { //식당게시판 정보 수정
				
				int maxSize = 1024*1024*10; //10m
				//savepath 경로 변경
				String savePath = "C:\\Users\\limdo\\git\\senasic6\\src\\main\\webapp\\Restaurant/RestImg";
				File filePath = new File(savePath);
				if(!filePath.exists()) {filePath.mkdir();}
				System.out.println(savePath);
				MultipartRequest multi = new MultipartRequest(request,savePath,maxSize,"UTF8",new DefaultFileRenamePolicy());
				RestBoardDTO real = dao.getRestBoardInfo(Integer.parseInt(multi.getParameter("seq")));
				
				multi.getParameter(savePath);
				String root = "\\Restaurant\\RestImg\\";

				String oriName1 = multi.getOriginalFileName("photo[0]");
				String sysName1 = multi.getFilesystemName("photo[0]");
				if(sysName1 ==null) {
					sysName1=real.getPhoto1().substring(root.length());
				}
				String oriName2 = multi.getOriginalFileName("photo[1]");
				String sysName2 = multi.getFilesystemName("photo[1]");
				if(sysName2 == null) {
					sysName2=real.getPhoto2().substring(root.length());
				}
				String oriName3 = multi.getOriginalFileName("photo[2]");
				String sysName3 = multi.getFilesystemName("photo[2]");
				if(sysName3 ==null) {
					sysName3=real.getPhoto3().substring(root.length());
				}
				String title = multi.getParameter("title");
				String ctg = multi.getParameter("category");
				String loc = multi.getParameter("location");
				String loc_detail = multi.getParameter("location_detail");
				String phone = multi.getParameter("phone");
				String hour = multi.getParameter("hour");
				String garage = multi.getParameter("garage");
				String link = multi.getParameter("link");
				String m1 = multi.getParameter("menu1");
				int p1 = Integer.parseInt(multi.getParameter("price1"));
				String m2 = multi.getParameter("menu2");
				int p2 = Integer.parseInt(multi.getParameter("price2"));
				String m3 = multi.getParameter("menu3");
				int p3 = Integer.parseInt(multi.getParameter("price3"));
				int result = dao.updateRest(title, loc, loc_detail, ctg, hour, garage, phone, root+sysName1, root+sysName2, root+sysName3, link, real.getSeq());
				int menu = dao.updateMenu(title, m1, p1, m2, p2, m3, p3, real.getTitle());
				request.getRequestDispatcher("/rbDetail.admin?num="+real.getSeq()).forward(request, response);
				
			}else if(cmd.equals("rbDelete.admin")){
				int seq = Integer.parseInt(request.getParameter("num"));
				RestBoardDTO real = dao.getRestBoardInfo(seq);
				int result = dao.deleteRest(seq);
				int menu = dao.deleteMenu(real.getTitle());
				response.sendRedirect("/rbEdit.admin");
				
			}else if(cmd.equals("member.admin")) {
				int currentPage=1;
				if(request.getParameter("cpage")!=null) {currentPage = Integer.parseInt(request.getParameter("cpage"));}
	            int pageTotalCount = rbdao.getPageTotalCount();
	            if(currentPage <1) {currentPage = 1;}
	            if(currentPage > pageTotalCount) {currentPage = pageTotalCount;}
	            int start = currentPage * Statics.ADMIN_COUNT_PER_PAGE - (Statics.ADMIN_COUNT_PER_PAGE-1);
	            int end = currentPage * Statics.ADMIN_COUNT_PER_PAGE;
				List<MemberDTO> list = dao.listMember(start, end);
				List<Integer> navi = dao.getMemberNavi(currentPage);
	             request.setAttribute("start", start-1);
	             request.setAttribute("end", end+1);
	             request.setAttribute("navi", navi);
	             request.setAttribute("cpage", currentPage);
				request.setAttribute("list", list);
				request.getRequestDispatcher("/admin/member.jsp").forward(request, response);
				
			}else if(cmd.equals("mEdit.admin")) {
				int num = Integer.parseInt(request.getParameter("num"));
				
				MemberDTO dto = dao.getMember(num);
				if(dto.getPh().length()==9) {
					request.setAttribute("phoneFirst", dto.getPh().substring(0,2));
					request.setAttribute("phoneMiddle", dto.getPh().substring(2,5));
					request.setAttribute("phoneLast", dto.getPh().substring(5, 9));									
				}else if(dto.getPh().length()==10 && dto.getPh().substring(0,2).equals("02")) {
					request.setAttribute("phoneFirst", dto.getPh().substring(0,2));
					request.setAttribute("phoneMiddle", dto.getPh().substring(2,6));
					request.setAttribute("phoneLast", dto.getPh().substring(6, 10));					
				}else if(dto.getPh().length()==10){
					request.setAttribute("phoneFirst", dto.getPh().substring(0,3));
					request.setAttribute("phoneMiddle", dto.getPh().substring(3,6));
					request.setAttribute("phoneLast", dto.getPh().substring(6, 10));										
				}else {
					request.setAttribute("phoneFirst", dto.getPh().substring(0,3));
					request.setAttribute("phoneMiddle", dto.getPh().substring(3,7));
					request.setAttribute("phoneLast", dto.getPh().substring(7, 11));					
				}
				request.setAttribute("dto", dto);
				request.getRequestDispatcher("/admin/memberEdit.jsp").forward(request, response);
				
			}else if(cmd.equals("modifyM.admin")) {
				
				int maxSize = 1024*1024*10; //10m
				//savepath 경로 변경
				String savePath = "C:\\Users\\limdo\\git\\senasic6\\src\\main\\webapp\\member/profileImg";
				File filePath = new File(savePath);
				if(!filePath.exists()) {filePath.mkdir();}
				System.out.println(savePath);
				MultipartRequest multi = new MultipartRequest(request,savePath,maxSize,"UTF8",new DefaultFileRenamePolicy());
//				RestBoardDTO real = dao.getRestBoardInfo(Integer.parseInt(multi.getParameter("seq"))); 멤버 수정후
				String root = "\\Restaurant\\RestImg\\";
				String oriName1 = multi.getOriginalFileName("photo");
				String sysName1 = multi.getFilesystemName("photo");
//				if(sysName1 ==null) {
//					sysName1=real.getPhoto1().substring(root.length());
//				}
				multi.getParameter(savePath);
				String id= multi.getParameter("id");
        		String nn = multi.getParameter("nn");
        		String m = multi.getParameter("m");
        		String p1 = multi.getParameter("phone1");
        		String p2 = multi.getParameter("phone2");
        		String p3 = multi.getParameter("phone3");
        		int age = Integer.parseInt(multi.getParameter("age"));
        		String gender = multi.getParameter("gender");
        		
        		
        		String ph = (p1+p2+p3);
        		
    			dao.modifyM(id,nn,m,age,gender,ph);
    			response.sendRedirect("/mEdit.admin");
        	   
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doGet(request, response);
	}

}
