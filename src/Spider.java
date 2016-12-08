import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import java.io.IOException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Spider {
    private static Scanner s;
    private static String word;
    
    public Spider(String word){
    	Spider.word=word;
    }

	public String[] search() throws IOException {
    
        CloseableHttpClient httpClient = HttpClients.createDefault();
     /*
        System.out.print("请输入你要查的单词:");
		s = new Scanner(System.in);
        String word = s.nextLine();
     */
        String wordForYoudao = word.replaceAll(" ","+");
        String wordForBaidu = word.replaceAll(" ","+");
        String wordForDict = word.replaceAll(" ","%20");
        

        //根据查找单词构造查找地址
        HttpGet getWordMeanFromYoudao = new HttpGet("http://dict.youdao.com/search?q=" + 
        		wordForYoudao + "&keyfrom=dict.index");
        HttpGet getWordMeanFromBaidu = new HttpGet("http://dict.baidu.com/s?wd=" + 
        		wordForBaidu + "&ptype=english");
        HttpGet getWordMeanFromDict = new HttpGet("http://dict.cn/" + wordForDict);
        
        CloseableHttpResponse responseFromYoudao = httpClient.execute(getWordMeanFromYoudao);//取得返回的网页源码
        CloseableHttpResponse responseFromBaidu = httpClient.execute(getWordMeanFromBaidu);
        CloseableHttpResponse responseFromDict = httpClient.execute(getWordMeanFromDict);

        String resultFromYoudao = EntityUtils.toString(responseFromYoudao.getEntity());
        String resultFromBaidu = EntityUtils.toString(responseFromBaidu.getEntity());
        String resultFromDict = EntityUtils.toString(responseFromDict.getEntity());
        responseFromYoudao.close();
        responseFromBaidu.close();
        responseFromDict.close();
        //注意(?s)，意思是让'.'匹配换行符，默认情况下不匹配
        String[] res = {"","",""};
        
        Pattern searchMeanPatternFromYoudao = 
        		Pattern.compile("(?s)<div class=\"trans-container\">.*?<ul>.*?</div>");
        Matcher m1FromYoudao = searchMeanPatternFromYoudao.matcher(resultFromYoudao); //m1是获取包含翻译的整个<div>的
        System.out.println("有道释义:");
        if (m1FromYoudao.find()) {
            String meansFromYoudao = m1FromYoudao.group();//所有解释，包含网页标签
            Pattern getChineseFromYoudao = Pattern.compile("(?m)<li>(.*?)</li>"); //(?m)代表按行匹配
            Matcher m2FromYoudao = getChineseFromYoudao.matcher(meansFromYoudao);
            
            
            //while (m2FromYoudao.find()) {
            if(m2FromYoudao.find()) {
                //在Java中(.*?)是第1组，所以用group(1)
            	
            	res[0]=m2FromYoudao.group(1);
                System.out.println("\t" + m2FromYoudao.group(1));
            }
        } else {
            System.out.println("未查找到释义.");
            res[0]="NoResult";
            System.exit(0);
        }
        
        
        Pattern searchMeanPatternFromBaidu = 
        		Pattern.compile("(?s)<div class=\"en-content\">.*?<div><p>.*?</div>");
        Matcher m1FromBaidu = searchMeanPatternFromBaidu.matcher(resultFromBaidu); 
        System.out.println("百度释义:");
        if (m1FromBaidu.find()) {
            String meansFromBaidu = m1FromBaidu.group();
            Pattern getChineseFromBaidu = Pattern.compile("(?m)<strong>(.*?)</strong><span>(.*?)</span>"); 
            Matcher m2FromBaidu = getChineseFromBaidu.matcher(meansFromBaidu);

            //while (m2FromBaidu.find()) {
            if (m2FromBaidu.find()){
            	res[1]=StringEscapeUtils.unescapeHtml(m2FromBaidu.group(1)) + 
                			StringEscapeUtils.unescapeHtml(m2FromBaidu.group(2));
                System.out.println("\t" +  
                		StringEscapeUtils.unescapeHtml(m2FromBaidu.group(1)) + 
                		StringEscapeUtils.unescapeHtml(m2FromBaidu.group(2)));
            }
        } else {
            System.out.println("未查找到释义.");
            res[1]="NoResult";
            System.exit(0);
        }
        
        Pattern searchMeanPatternFromDict = Pattern.compile("(?s)<div class=\"basic clearfix\">.*?<li>.*?</ul>");
        Matcher m1FromDict = searchMeanPatternFromDict.matcher(resultFromDict); 
        System.out.println("海词词典释义:");
        if (m1FromDict.find()) {
            String meansFromDict = m1FromDict.group();//所有解释，包含网页标签
            Pattern getChineseFromDict = 
            		Pattern.compile("(?m)<li>(<span>(.*?)</span>)?<strong>(.*?)</strong></li>"); 
            
            Matcher m2FromDict = getChineseFromDict.matcher(meansFromDict);

            //while (m2FromDict.find()) {
            if  (m2FromDict.find()) {
            	System.out.print("\t");
            	for(int i=1;i<=m2FromDict.groupCount();i++){
            		if(StringEscapeUtils.unescapeHtml(m2FromDict.group(i))!=null
            				&&
            			!StringEscapeUtils.unescapeHtml(m2FromDict.group(i)).startsWith("<")) {
            			
            			System.out.print(StringEscapeUtils.unescapeHtml(m2FromDict.group(i)));
            			res[2] = res[2]+StringEscapeUtils.unescapeHtml(m2FromDict.group(i));
            		}
            		
            	}
            	System.out.println();
            }
        } else {
            System.out.println("未查找到释义.");
            res[2]="NoResult";
            System.exit(0);
        }
    
	return res;
	}
}
