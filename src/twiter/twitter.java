package twiter;

import java.io.BufferedReader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;


public class twitter {

	public static Twitter twitter;

	private String token;

	private String[] Palabras_clave;

	private PrintWriter pw_salida;

	private long total_solicitudes;
	private String url_syso;
	
	public twitter() throws java.text.ParseException, TwitterException{

		try {

			generarArchivoDeSalida();

			leerInfo();

			pw_salida.close();
			System.out.println("Proceso terminado " + total_solicitudes);
		}
		catch (IOException e) {
			e.printStackTrace();
		}


	}

	public String generarFechaDeExtraccion(){
		Date fecha_extraccion = new Date();
		SimpleDateFormat df =new SimpleDateFormat("yyyy-MM-dd");
		return df.format(fecha_extraccion);
	}

	public void generarArchivoDeSalida() throws IOException{
		String fileName = "C:/Users/User/Documents/Lina Moros/twitter" + "-" + generarFechaDeExtraccion() + ".csv";
		pw_salida = new PrintWriter(new FileWriter(fileName,false));
	}

	public BufferedReader generarArchivoDeLecturaDeInformacion() throws FileNotFoundException{
		BufferedReader info = new BufferedReader( new FileReader(new File("C:/Users/User/Documents/Lina Moros/twitter-info.csv")));
		return info;
	}

	public void leerInfo() throws IOException, java.text.ParseException, TwitterException{
		BufferedReader info = generarArchivoDeLecturaDeInformacion();
		Palabras_clave = info.readLine().split(",");
		escribirTitulosColumnas();
		crearTodasLasSolicitudes(info);
	}

	public String extraerNombreDelActor(String info_actor) throws MalformedURLException{
		return info_actor.split(",")[0];
	}

	/**
	 * @param info_actor
	 * @return
	 */
	public String extraerIdDeLaPagina(String info_actor){
		return info_actor.split(",")[1];
	}

	public void crearTodasLasSolicitudes(BufferedReader info) throws IOException, TwitterException, java.text.ParseException {
		String info_actor = info.readLine();
		int i = 0;
		while(info_actor != null && !	info_actor.equals("")){
			String actor = extraerNombreDelActor(info_actor);
			String page = extraerIdDeLaPagina(info_actor);
			int publicaciones_actor = 0;
			generarSolicitudes(page);
			info_actor = info.readLine();
		}
	}

	public void escribirTitulosColumnas() throws IOException{
		pw_salida.println("Actor,Fuente,Fecha de la publica,Publicacion\n");		
	}






	public void generarSolicitudes(String page) throws TwitterException, java.text.ParseException{
		boolean terminado = false;
		int total = 0;
		Paging paging = new Paging(1,200);
		while(!terminado){
			List<Status> statuses = twitter.getUserTimeline(page, paging);
			total += statuses.size();
			long lastId;
			for(int i = 0; i< statuses.size(); i++){
				Status status = statuses.get(i);
				escribirInformacion(status, page);
			}
			if(statuses.size() == 0){
				terminado = true;
			}
			else {
				lastId = statuses.get(statuses.size()-1).getId();
				paging.setMaxId(lastId - 1);
			}
		}
		System.out.println(page + " " + total);
	}

	private void escribirInformacion(Status status, String actor) throws java.text.ParseException {
		Date fecha_Twitter = status.getCreatedAt();
		Date date = new Date(fecha_Twitter.getYear(), fecha_Twitter.getMonth(), fecha_Twitter.getDay());
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		String fecha = formatter.format(date);
		
		String contenido = status.getText();
		
		boolean contiene = false;
		CharSequence cs;
		
		for(int j = 0; j < Palabras_clave.length && !contiene; j++){
			String palabra = Palabras_clave[j];
			cs = palabra;
			if(org.apache.commons.lang3.StringUtils.containsIgnoreCase(contenido,cs)){
				contiene = true;
			}
			
			
		}
		if(contiene) {
			CharSequence cs2 = ",";
			cs = "\n";
			if(contenido.contains(cs)){
				contenido = contenido.replace(cs, ". ");
			}
			if(contenido.contains(cs2)){
				contenido = contenido.replace(cs2, "; ");
			}
			System.out.println(contenido);
			pw_salida.write(actor + ",Twitter," + fecha + "," + contenido + "\n" );
		}	
		
	}
	
	public static void main(String[] args) throws TwitterException {
		ConfigurationBuilder configBuilder = new ConfigurationBuilder();
		configBuilder.setTweetModeExtended(true);
		configBuilder.setDebugEnabled(true)
		.setOAuthConsumerKey("vuSBLc2AgS30JLTDmrkbIjfFf")
		.setOAuthConsumerSecret("QtUPyk7qNvY7ePqS6klCHuptQTezMKkzGm1KUAysISxc1WFkP6")
		.setOAuthAccessToken("881969698300190720-CKiZL1ZsQib1AZHJAZ6ihfc27MmsoVX")
		.setOAuthAccessTokenSecret("ezCP853iLxK3RilX1Fh8LSQZUIc4gRkfglsSdFVyPsku7");

		TwitterFactory tf = new TwitterFactory(configBuilder.build());
		twitter = tf.getInstance();

		try {
			twitter t = new twitter();
		} catch (java.text.ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
