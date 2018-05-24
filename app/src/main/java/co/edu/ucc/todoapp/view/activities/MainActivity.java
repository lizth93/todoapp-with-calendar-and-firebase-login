package co.edu.ucc.todoapp.view.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import co.edu.ucc.todoapp.R;
import co.edu.ucc.todoapp.data.entidades.mapper.TaskEntityMapper;
import co.edu.ucc.todoapp.data.repository.task.TaskDataSourceFactory;
import co.edu.ucc.todoapp.data.repository.task.TaskRepository;
import co.edu.ucc.todoapp.domain.usecase.InteractorTask;
import co.edu.ucc.todoapp.domain.usecase.task.AddTaskUseCase;
import co.edu.ucc.todoapp.domain.usecase.task.GetAllTaskUseCase;
import co.edu.ucc.todoapp.view.adapters.TaskAdapter;
import co.edu.ucc.todoapp.view.presenter.IMainPresenter;
import co.edu.ucc.todoapp.view.presenter.MainPresenter;
import co.edu.ucc.todoapp.view.viewmodels.TaskViewModel;
import co.edu.ucc.todoapp.view.viewmodels.mapper.TaskViewModelMapper;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements IMainView {

    private EditText Name;
    private EditText Password;
    private TextView Info;
    private Button Login;
    private int counter = 5;
    private TextView userRegistration;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;
    private TextView forgotPassword;

    @BindView(R.id.rvTask)
    RecyclerView rvTask;

    private IMainPresenter presenter;
    private TaskAdapter taskAdapter;
    private final static int taskCalendar = 0;
    EditText Tarea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Name = (EditText) findViewById(R.id.etName);
        Password = (EditText) findViewById(R.id.etPassword);
        Info = (TextView) findViewById(R.id.tvInfo);
        Login = (Button) findViewById(R.id.btnLogin);
        userRegistration = (TextView) findViewById(R.id.tvRegister);
        forgotPassword = (TextView) findViewById(R.id.tvForgotPassword);

        Info.setText("No of attempts remaining: 5");
        firebaseAuth = FirebaseAuth.getInstance();
        progressDialog = new ProgressDialog(this);

        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user != null) {
            finish();
            startActivity(new Intent(MainActivity.this, CalendarActivity.class));
        }

        Login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validate(Name.getText().toString(), Password.getText().toString());
            }
        });
        userRegistration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RegistrationActivity.class));
            }
        });

        forgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, PasswordActivity.class));
            }
        });
    }
    private void validate(String userName, String userPassword) {

        progressDialog.setMessage("You can subscribe to my channel until you are verified!");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(userName, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    progressDialog.dismiss();
                    //Toast.makeText(MainActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();
                    checkEmailVerification();
                } else {
                    Toast.makeText(MainActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();
                    counter--;
                    Info.setText("No of attempts remaining: " + counter);
                    progressDialog.dismiss();
                    if (counter == 0) {
                        Login.setEnabled(false);
                    }
                }
            }

            private void checkEmailVerification() {
                FirebaseUser firebaseUser = firebaseAuth.getInstance().getCurrentUser();
                Boolean emailflag = firebaseUser.isEmailVerified();

                startActivity(new Intent(MainActivity.this, CalendarActivity.class));

//        if(emailflag){
//            finish();
//            startActivity(new Intent(MainActivity.this, SecondActivity.class));
//        }else{
//            Toast.makeText(this, "Verify your email", Toast.LENGTH_SHORT).show();
//            firebaseAuth.signOut();
//        }
            }
        });




        ButterKnife.bind(this);

        presenter = new MainPresenter(
                this,
                new AddTaskUseCase(
                        Schedulers.io(),
                        AndroidSchedulers.mainThread(),
                        new TaskRepository(
                                new TaskEntityMapper(),
                                new TaskDataSourceFactory()
                        )
                ),
                new GetAllTaskUseCase(
                        Schedulers.io(),
                        AndroidSchedulers.mainThread(),
                        new TaskRepository(
                                new TaskEntityMapper(),
                                new TaskDataSourceFactory()
                        )
                ),
                new InteractorTask(),
                new TaskViewModelMapper()
        );

        initAdapter();
        initRecyclerView();

    }

    private void initAdapter() {
        taskAdapter = new TaskAdapter();
    }

    private void initRecyclerView() {
        rvTask.setLayoutManager(new LinearLayoutManager(this));
        rvTask.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(this,
                DividerItemDecoration.VERTICAL));
        rvTask.setAdapter(taskAdapter);
    }

    @Override
    public void showError(String error) {
        // Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showSuccessful() {
        // Toast.makeText(this, R.string.msg_successful, Toast.LENGTH_SHORT).show();
    }

    @OnClick(R.id.fbAddTask)
    @Override
    public void addTask() {
        // Crea intento para recolectar datos en otra vista
        Intent i = new Intent(this, CalendarActivity.class);
        startActivityForResult(i,taskCalendar);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Comprobamos si el resultado de la segunda actividad es "RESULT_CANCELED".
        if (resultCode == RESULT_CANCELED) {
            // Si es así mostramos mensaje de cancelado por pantalla.
            Toast.makeText(this, "Creación de tarea cancelada", Toast.LENGTH_SHORT).show();
        } else {
            // De lo contrario, recogemos el resultado de la segunda actividad.

            // Obtiene los datos enviados por la actividad creada
            String taskName = data.getExtras().getString("taskName");
            String selectedDate = data.getExtras().getString("selectedDate");

            // Se agrega la tarea a la lista
            // ...
            presenter.addTask(taskName + " / Fecha: " + selectedDate);

            // Trea agregada a la lista, se muestra mensaje confirmado el hecho
            Toast.makeText(this, "Nueva tarea creada", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showTasks(List<TaskViewModel> taskViewModels) {
        taskAdapter.addLstTask(taskViewModels);
        taskAdapter.notifyDataSetChanged();
    }
}