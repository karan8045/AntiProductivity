package com.karan.antiproductivity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.karan.antiproductivity.ui.MainViewModel
import com.karan.antiproductivity.ui.navigation.NavGraph
import com.karan.antiproductivity.ui.theme.AntiProductivityTheme

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AntiProductivityTheme {
                NavGraph(viewModel = viewModel)
            }
        }
    }
}
