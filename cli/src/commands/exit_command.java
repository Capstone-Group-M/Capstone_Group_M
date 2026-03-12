package cli.src.commands;

public class exit_command implements commands {

    @Override
    public void execute(String[] args) {
        System.out.println("Exiting...");
        System.exit(0);
    }
}