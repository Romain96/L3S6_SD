import java.rmi.server.UnicastRemoteObject ;
import java.rmi.RemoteException ;
import java.rmi.* ; 
import java.net.MalformedURLException ; 
import java.util.*;
import java.util.Timer;

public class ProducteurImpl extends UnicastRemoteObject implements Producteur
{  
	//==================================================================
	//							Attributs
	//==================================================================
	
	private int idProducteur;		// identifiant unique d'un producteur
	private String typeRessource;	// pour l'instant un seule ressource
	private int quantiteRessource;	// idem
	
	//==================================================================
	//							Constructeur
	//==================================================================
	
	public ProducteurImpl(int idProducteur, String typeRessource, int quantiteRessource) throws RemoteException
	{
		this.idProducteur = idProducteur;
		this.typeRessource = typeRessource;
		this.quantiteRessource = quantiteRessource;
		// DEBUG
		System.out.println("Producteur init : " + idProducteur  + " " + typeRessource + " " + quantiteRessource );
	}
	
	//==================================================================
	//							Getters
	//==================================================================
	
	// retourne l'id du producteur
	public int getIdProducteur()
	{
		return this.idProducteur;
	}
	
	// retourne la ressource produite par le producteur
	public String getTypeRessource()
	{
		return typeRessource;
	}
	
	// retourne la quantité courante de cette ressource possédée
	public int getQuantiteRessource()
	{
		return quantiteRessource;
	}
	
	//==================================================================
	//							Setters
	//==================================================================
	
	// positionne l'id du producteur
	public void setIdProducteur(int idProducteur)
	{
		this.idProducteur = idProducteur;
	}
	
	// positionne le type de ressource produite par le producteur
	public void setTypeRessource(String typeRessource)
	{
		this.typeRessource = typeRessource;
	}
	
	// positionne la quantité de ressources possédée par le producteur
	public void setQuantiteRessource(int quantiteRessource)
	{
		this.quantiteRessource = quantiteRessource;
	}
	
	//==================================================================
	//							Méthodes
	//==================================================================
	
	/*
	 * Fonction 	: attribuerRessources
	 * Argument(s)	: la quantité de ressource demandée par l'agent demandeur
	 * Résultat(s)	: la quantité de ressource attribuée par le producteur (>= 0 et <= quantité demandée)
	 * Commentaires	: /
	 */
	public int attribuerRessources(String typeRessource, int quantiteDemandee) throws RemoteException
	{
		// si le type de ressource n'est pas produit par le producteur
		if (!this.typeRessource.equals(typeRessource))
		{
			return 0;
		}
		
		int quantiteAttribuee = 0;
		// si la quantité demandée est supérieure à la quantité disponible
		// l'agent ne reçoit que le maximum disponible
		if (this.quantiteRessource <= quantiteDemandee)
		{
			quantiteAttribuee = this.quantiteRessource;
			this.quantiteRessource = 0;
			return quantiteAttribuee;
		}
		else
		{
			quantiteAttribuee = quantiteDemandee;
			this.quantiteRessource -= quantiteDemandee;
			return quantiteAttribuee;
		}
	}
	
	/*
	 * Fonction 	: observer
	 * Argument(s)	: l'id de l'agent demandeur
	 * Résultat(s)	: la ressource produite (type, quantité)
	 * Commentaires	: /
	 */
	public Ressource observer(int idAgent) throws RemoteException
	{
		Ressource ressourceProduite = new Ressource(this.typeRessource, this.quantiteRessource, 0);	// 0 pour l'objectif
		System.out.println("Producteur " + getIdProducteur() + " : l'agent " + idAgent + " m'observe");
		return ressourceProduite;
	}
	
	/*
	 * Fonction 	: genererRessources
	 * Argument(s)	: /
	 * Résultat(s)	: /
	 * Commentaires	: régénère les ressources suivant une certaine politique
	 */
	public void genererRessources()
	{
		System.out.println("Producteur " + getIdProducteur() + " : je régénère mes ressources");
		// très simple pour l'instant on double ne nombre restant (si > 0)
		if (this.quantiteRessource > 0)
		{
			this.quantiteRessource *= 2;
		}
		else
		{
			this.quantiteRessource = 100;	// nombre arbitraire
		}
	}
	
	/*
	 * Fonction 	: terminerJeu
	 * Argument(s)	: /
	 * Résultat(s)	: /
	 * Commentaires	: appelé par le coordinateur pour terminer le producteur
	 */
	public void terminerJeu() throws RemoteException
	{
		try
		{
			// unbind avant la suppression
			Naming.unbind("rmi://localhost:9000/producteur" + getIdProducteur());

			// supprime du runtime RMI
			UnicastRemoteObject.unexportObject(this, true);
			
			System.out.println("Producteur " + idProducteur + " se termine" );
		} catch(Exception e){System.out.println(e);}
		System.exit(0);
	}
	
	public static void main(String [] args)
	{
		if (args.length != 4)
		{
			System.out.println("Usage : java ObjectProducteur <port rmiregistry> <idProducteur> <typeRessource> <quantiteRessource>") ;
			System.exit(0) ;
		}
		try
		{
			System.out.println("args : " + args[0]+ "  " + args[1] + " " + args[2] + " " + args[3]);
			Coordinateur coordinateur = (Coordinateur) Naming.lookup( "rmi://localhost:" + args[0] + "/coordinateur" );
			
			int idProducteur = Integer.parseInt(args[1]);
			int quantiteRessource = Integer.parseInt(args[3]);
			final ProducteurImpl objLocal = new ProducteurImpl(idProducteur, args[2], quantiteRessource );
			Naming.rebind( "rmi://localhost:" + args[0] + "/producteur" + args[1] ,objLocal) ;
			System.out.println("Producteur " + objLocal.getIdProducteur() + " pret") ;
			
			// s'enregistrer auprès du coordinateur (convention : port 9000)
			coordinateur.identifierProducteur(objLocal.getIdProducteur());

			Timer timer = new Timer();
			timer.schedule(new TimerTask() 
			{
				public void run() 
				{
					objLocal.genererRessources();
				}
			}, 0, 1000);
		}
		catch (NotBoundException re) { System.out.println(re) ; }
		catch (RemoteException re) { System.out.println(re) ; }
		catch (MalformedURLException e) { System.out.println(e) ; }
	}
}
