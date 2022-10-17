/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Project/Maven2/JavaApp/src/main/java/${packagePath}/${mainClassName}.java to edit this template
 */

package fr.insa.ugo.encheres;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Month;
import java.util.HashSet;

/**
 *
 * @author Ugo
 */
public class Encheres {

    public static void main(String[] args) {
        try ( Connection con = defautConnect()) {
            LocalDate ld = LocalDate.of(1985, Month.MARCH, 23);
            java.sql.Date sqld = java.sql.Date.valueOf(ld);
            //creeSchema(con);
            //createPerson(con, "Toto", sqld);
            //afficheToutesPersonnes(con);
            System.out.println("connecté !!!");
        } catch (Exception ex) {
            throw new Error("Probleme:" + ex);
        }
    }
    
    public static Connection connectGeneralPostGres(String host, int port, String database, String user, String pass)
            throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver"); // test de l'existence du driver postgre SQL
        Connection con = DriverManager.getConnection("jdbc:postgresql://" + host + ":" + port + "/" + database, user, pass); // ordre de connection
        // fixe le plus haut degré d'isolation entre transactions (jsp ce que ça veut dire)
        con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
        return con;
    }

    public static Connection defautConnect()
            throws ClassNotFoundException, SQLException {
        return connectGeneralPostGres("localhost", 5439, "postgres", "postgres", "pass");
    }
    
    public static void createTablePerson (Connection con) throws SQLException {
        try (Statement st = con.createStatement()){
            
            //on veut que le schéma soit entièrement créé ou pas du tout
            // il nous faut plusieurs ordres pour créer le schéma
            // on va donc explicitement gérer les connection
            
            con.setAutoCommit(false);
            
            st.executeUpdate("""
                            create table Person(
                            id integer primary key generated akways as identity,
                            nom varchar(50) not null,
                            dateNaissance date)
                            """);
            
            st.executeUpdate("""
                             create table Surnom(
                             id integer primary key generated always as identity,
                             nom varchar(50) not null)
                             """);
            
            st.executeUpdate("""
                            create table PersonSurnoms(
                            idPerson integer,
                            idSurnom integer,)
                       """);
            
            st.executeUpdate("""
                             alter table PersonSurnoms
                             add constraint fk_Person_Surnoms_idPerson
                             foreign key(idPerson)
                             references Person(id)
                             on delete restrict
                             on update restrict
                             """);
            
            st.executeUpdate("""
                             alter table PersonSurnoms
                             add constraint fk_Person_Surnoms_idSurnom
                             foreign key(idSurnom)
                             references Surnom(id)
                             on delete restrict
                             on update restrict
                             """);
            
            // si j'arrive ici, c'est que tout s'est bien passé
            // je valide alors la transaction
            con.commit();
        }catch (SQLException ex){
            // si qqch se passe mal, j'annule la transaction
            // avant de resignaler l'ecxeption
            con.rollback();
            throw ex;
        }finally{
            // pour s'assurer que le autoCommit(true) reste le comportement par défaut (utile dans la plupart des "select"
            con.setAutoCommit(true);
            
        }
    }
    
    public static void createPerson (Connection con, String nom, java.sql.Date dateNaiss) throws SQLException{
        try (PreparedStatement pst = con.prepareStatement(
        """
        insert into Person (nom, sateNaissance)
        values(?,?)
        """)){
            pst.setString(1, nom);
            pst.setDate(2,dateNaiss);
            pst.executeUpdate();
        }
    }
    
    
    public static void creeSchema(Connection con)
            throws SQLException {
        // je veux que le schema soit entierement créé ou pas du tout
        // je vais donc gérer explicitement une transaction
        con.setAutoCommit(false);
        try ( Statement st = con.createStatement()) {
            // creation des tables
            st.executeUpdate(
                    """
                    create table utilisateur (
                        id integer not null primary key
                        generated always as identity,
                    -- ceci est un exemple de commentaire SQL :
                    -- un commentaire commence par deux tirets,
                    -- et fini à la fin de la ligne
                    -- cela me permet de signaler que le petit mot clé
                    -- unique ci-dessous interdit deux valeurs semblables
                    -- dans la colonne des noms.
                        nom varchar(30) not null unique,
                        pass varchar(30) not null
                    )
                    """);
            // si j'arrive jusqu'ici, c'est que tout s'est bien passé
            // je confirme (commit) la transaction
            con.commit();
            // je retourne dans le mode par défaut de gestion des transaction :
            // chaque ordre au SGBD sera considéré comme une transaction indépendante
            con.setAutoCommit(true);
        } catch (SQLException ex) {
            // quelque chose s'est mal passé
            // j'annule la transaction
            con.rollback();
            // puis je renvoie l'exeption pour qu'elle puisse éventuellement
            // être gérée (message à l'utilisateur...)
            throw ex;
        } finally {
            // je reviens à la gestion par défaut : une transaction pour
            // chaque ordre SQL
            con.setAutoCommit(true);
        }
    }
    
    public static void afficheToutesPersonnes (Connection con)
            throws SQLException {
        try (Statement st = con.createStatement()){
            ResultSet res = st.executeQuery(
            "select * from person");
            while (res.next()){
                // on peut accéder à une colonne par son nom
                int id = res.getInt("id");
                String nom = res.getString("nom");
                // on peut aussi y accéder par son numéro
                // pas de num 0 ça commence à 1
                java.sql.Date dn = res.getDate(3);
                System.out.println(id + " : " + nom + " né le " + dn);
            }
        }
    }

}
