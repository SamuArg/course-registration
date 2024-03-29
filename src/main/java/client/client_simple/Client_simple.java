package client.client_simple;

import server.models.Course;
import server.models.RegistrationForm;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Représente un client simple qui fonctionne dans le terminal
 */
public class Client_simple {
    private Socket clientSocket;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    private ArrayList<Course> courses;

    /**
     * Cette méthode permet de récupérer le stream d'input et d'output du clientSocket
     */
    public void run(){
        try{
            objectOutputStream = new ObjectOutputStream(clientSocket.getOutputStream());
            objectInputStream = new ObjectInputStream(clientSocket.getInputStream());


        } catch (IOException e) {
            System.err.println("Erreur serveur.");
        }
    }

    /**
     * Cette méthode demande à l'utilisateur de choisir quelle session veut-il voir la liste de cours
     */
    public void choice(){
        try{
            System.out.println("Veuillez choisir la session pour laquelle vous voulez consulter la liste de cours:");
            System.out.println("1. Automne");
            System.out.println("2. Hiver");
            System.out.println("3. Ete");
            Scanner scan = new Scanner(System.in);
            int choice = scan.nextInt();

            if (choice == 1){
                System.out.println("> Choix: 1");
                printCourses("Automne");
            }
            else if (choice == 2){
                System.out.println("> Choix: 2");
                printCourses("Hiver");
            }

            else if (choice == 3){
                System.out.println("> Choix: 3");
                printCourses("Ete");
            }
            else{
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Veuillez rentrer une valeur valide.");
            choice();
        }
    }

    /**
     * Cette méthode permet de recevoir les cours d'une session depuis le serveur et d'imprimer les cours à l'utilisateur
     * @param session Définie quelle est la session que l'utiliseur veut se renseigner
     */
    public void printCourses(String session){
        try{
            connect();
            run();
            objectOutputStream.writeObject("CHARGER " + session);
            courses = (ArrayList<Course>) objectInputStream.readObject();
            System.out.println("Les cours offerts pendant la session d'"+session+" sont:");
            for(int i=0;i<courses.size();i++){
                System.out.println(i + ". "+courses.get(i).getCode()+"\t"+courses.get(i).getName());
            }
            nextChoice();
        } catch (IOException e) {
            System.err.println("Erreur serveur.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cette méthode permet de demander à l'utilisateur la prochaine chose qu'il veut faire
     */
    public void nextChoice(){
        try{
            System.out.println("1. Consulter les cours offerts pour une autre session");
            System.out.println("2. Inscription à un cours");
            Scanner scan = new Scanner(System.in);
            int choice = scan.nextInt();
            if(choice == 1){
                System.out.println("> Choix: 1");
                choice();
            }
            else if (choice == 2){
                System.out.println("> Choix: 2");
                createForm();
                nextChoice();
            }
            else {
                throw new IllegalArgumentException();
            }
        } catch (IllegalArgumentException e) {
            System.out.println("Veuillez rentrer une valeur valide.");
            nextChoice();
        }
    }

    /**
     * Cette méthode permet de créer le RegistrationForm pour que l'utilisateur s'inscrive à un cours et l'envoie au server
     */
    public void createForm(){
        Scanner scan = new Scanner(System.in);
        String[] datas = new String[5];
        try{
            System.out.println("Veuillez saisir votre prénom: ");
            datas[0] = scan.next();
            System.out.println("Veuillez saisir votre nom: ");
            datas[1] = scan.next();
            System.out.println("Veuillez saisir votre email: ");
            datas[2] = scan.next();
            System.out.println("Veuillez saisir votre matricule: ");
            datas[3] = scan.next();
            System.out.println("Veuillez saisir le code du cours: ");
            datas[4] = scan.next();

            if (!isMatricule(datas[3])){
                throw new IllegalArgumentException();
            }

            else if(!isEmail(datas[2])){
                throw new IllegalArgumentException();
            }

                for (Course cours : courses) {
                    if (cours.getCode().equals(datas[4])) {
                        inscription(datas, cours);
                        String success = (String) objectInputStream.readObject();
                        System.out.println(success);
                        return;
                    }
                }
            throw new IllegalArgumentException();
        } catch (IllegalArgumentException e) {
            System.out.println("Veuillez fournir des informations exactes.");
            nextChoice();
        } catch (IOException e) {
            System.err.println("Erreur serveur.");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cette méthode permet à l'utilisateur de s'inscrire à un cours
     * @param datas Array d'informations sur l'utilisateur qui veut s'inscrire à un cours
     * @param course Cours pour lequel l'utilisateur veut s'inscrire
     */
    public void inscription(String[] datas, Course course){
        try{
            RegistrationForm form = new RegistrationForm(datas[0],datas[1],datas[2],datas[3],course);
            connect();
            run();
            objectOutputStream.writeObject("INSCRIRE");
            objectOutputStream.writeObject(form);
        } catch (IOException e) {
            System.out.println("Erreur serveur.");
        }
    }

    /**
     * Cette méthode permet de vérifier si une String respecte le format d'un matricule
     * @param input La String qu'il faut vérifier le format
     * @return Retourne true si la String respecte le format, false sinon
     */
    public boolean isMatricule(String input){
        Pattern matriculeRegex = Pattern.compile("[0-9]{8}");
        Matcher matcher = matriculeRegex.matcher(input);
        return matcher.find();
    }

    /**
     * Cette méthode vérifie si le email rentré par l'utilisateur est dans le bon format
     * @param input Le String correspond au email rentré
     * @return Return true si le email est dans le bon format, false sinon
     */
    public boolean isEmail(String input){
        Pattern emailRegex = Pattern.compile("\\S+@\\S+\\.\\S+");
        Matcher matcher = emailRegex.matcher(input);
        return matcher.find();
    }

    /**
     * Cette méthode permet à un client de se connecter au server pour faire une requête
     * @throws IOException S'il se passe une erreur lors de la connection entre le client et le server
     */
    public void connect() throws IOException {
        clientSocket = new Socket("127.0.0.1", 1337);
    }
}
