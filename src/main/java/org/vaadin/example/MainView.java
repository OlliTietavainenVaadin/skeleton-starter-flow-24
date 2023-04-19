package org.vaadin.example;

import com.vaadin.flow.component.AbstractField;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.grid.AbstractGridMultiSelectionModel;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.selection.MultiSelectionEvent;
import com.vaadin.flow.data.selection.SelectionEvent;
import com.vaadin.flow.data.selection.SelectionListener;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.shared.Registration;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a notification.
 */
@Route("")
public class MainView extends VerticalLayout {

    public static class MultiSelectAllEvent<C extends Component, T> extends MultiSelectionEvent<C, T> {

        public MultiSelectAllEvent(C listing, HasValue<AbstractField.ComponentValueChangeEvent<C, Set<T>>, Set<T>> source, Set<T> oldSelection, boolean userOriginated) {
            super(listing, source, oldSelection, userOriginated);
        }
    }

    public interface SelectAllListener<C extends Component, T> extends Serializable, EventListener {
        void allSelected(MultiSelectAllEvent<C, T> multiSelectAllEvent);
    }

    public static class CustomGrid<T> extends Grid<T> {

        public Registration addSelectAllListener(SelectAllListener<CustomGrid<T>, T> listener) {
            Objects.requireNonNull(listener, "listener cannot be null");
            return ComponentUtil.addListener(this, MultiSelectAllEvent.class,
                    (ComponentEventListener) (event -> listener
                            .allSelected((MultiSelectAllEvent) event)));
        }

        public CustomGrid() {
            setSelectionModel(new CustomMultiSelectionModel<>(this),
                    SelectionMode.MULTI);

        }

        class CustomMultiSelectionModel<T>
                extends AbstractGridMultiSelectionModel<T> {
            public CustomMultiSelectionModel(CustomGrid<T> grid) {
                super(grid);
                setSelectAllCheckboxVisibility(SelectAllCheckboxVisibility.VISIBLE);
            }

            @Override
            protected void fireSelectionEvent(
                    SelectionEvent<Grid<T>, T> event) {
                ((CustomGrid) event.getSource()).fireSelectionEvent(event);
            }

            @Override
            protected void clientSelectAll() {
                getSelectionColumn().setSelectAllCheckboxState(true);
                getSelectionColumn()
                        .setSelectAllCheckboxIndeterminateState(false);
                fireSelectionEvent(new MultiSelectAllEvent<>(getGrid(),
                        getGrid().asMultiSelect(), null, true));
            }

        }

        void fireSelectionEvent(SelectionEvent<Grid<T>, T> event) {

            super.fireEvent((ComponentEvent<Grid<?>>) event);
        }

    }

    List<String> data = new ArrayList<>();

    public MainView() {
        for (int i = 0; i < 10000; i++) {
            data.add("" + i);
        }
        // Use TextField for standard text input
        TextField textField = new TextField("Your name");
        textField.addClassName("bordered");

        Button button = new Button("Say hello", e -> {

        });
        CustomGrid<String> grid = new CustomGrid<>();
        grid.addColumn(s -> s).setHeader("value");
        grid.setItems(query ->
                {

                    return data.stream().skip(query.getOffset()).limit(query.getLimit());
                }
                , query -> data.size());
        grid.addSelectionListener(new SelectionListener<Grid<String>, String>() {
            @Override
            public void selectionChange(SelectionEvent<Grid<String>, String> selectionEvent) {
                Notification.show("Regular MultiSelectionEvent");

            }
        });
        grid.addSelectAllListener(new SelectAllListener<CustomGrid<String>, String>() {
            @Override
            public void allSelected(MultiSelectAllEvent<CustomGrid<String>, String> multiSelectAllEvent) {
                Notification.show("All selected!");
            }
        });

        add(textField, button, grid);
    }


}
