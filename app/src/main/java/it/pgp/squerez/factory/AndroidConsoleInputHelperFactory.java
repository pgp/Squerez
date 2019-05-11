package it.pgp.squerez.factory;

import org.gudy.azureus2.ui.console.CommandReader;
import org.gudy.azureus2.ui.console.ConsoleInputHelperFactory;
import org.gudy.azureus2.ui.console.commands.IConsoleCommand;

import java.io.Reader;

import it.pgp.squerez.commands.UIShowCommand;
import it.pgp.squerez.utils.StringQueueCommandReader;

public class AndroidConsoleInputHelperFactory extends ConsoleInputHelperFactory {

    @Override
    public CommandReader getCommandReader(Reader _in) {
        currentCommandReader = new StringQueueCommandReader(_in);
        return currentCommandReader;
    }

    @Override
    public CommandReader getEmptyCommandReader() {
        currentCommandReader = StringQueueCommandReader.closedReader;
        return currentCommandReader;
    }

    @Override
    public IConsoleCommand getShowCommand() {
        return new UIShowCommand();
    }
}
