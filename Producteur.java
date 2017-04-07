import java.rmi.Remote ; 
import java.rmi.RemoteException ; 

public interface Producteur extends Remote
{
	// appelé par le cordinateur à la fin du jeu pour terminer le producteur
	public void terminerJeu() throws RemoteException;
	
	// appelé par les agents pour acquérir des ressources
	public int attribuerRessources(int quantiteDemandee) throws RemoteException;
}
