package com.example.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.ui.theme.CatEducationColor
import com.example.ui.theme.CatEventColor
import com.example.ui.theme.CatFamilyColor
import com.example.ui.theme.CatFinancialColor
import com.example.ui.theme.CatHealthColor
import com.example.ui.theme.CatHomeColor
import com.example.ui.theme.CatPersonalColor
import com.example.ui.theme.CatVehicleColor
import com.example.ui.theme.CatWorkColor

enum class FileCategory(
    val id: String,
    val banglaName: String,
    val englishName: String,
    val icon: ImageVector,
    val color: Color,
    val defaultTags: List<String>
) {
    FINANCIAL(
        id = "financial",
        banglaName = "আর্থিক",
        englishName = "Financial",
        icon = Icons.Default.AccountBalance,
        color = CatFinancialColor,
        defaultTags = listOf("বিল", "রসিদ", "ব্যাংক স্টেটমেন্ট", "ট্যাক্স", "লোন", "বেতন")
    ),
    HEALTH(
        id = "health",
        banglaName = "স্বাস্থ্য",
        englishName = "Health",
        icon = Icons.Default.MedicalServices,
        color = CatHealthColor,
        defaultTags = listOf("প্রেসক্রিপশন", "টেস্ট রিপোর্ট", "মেডিকেল বিল", "ভ্যাকসিন", "ডায়াগনস্টিক")
    ),
    WORK(
        id = "work",
        banglaName = "অফিস ও কাজ",
        englishName = "Work & Office",
        icon = Icons.Default.Work,
        color = CatWorkColor,
        defaultTags = listOf("চুক্তি", "অফার লেটার", "প্রজেক্ট ফাইল", "মিটিং নোট", "সার্টিফিকেশন")
    ),
    FAMILY(
        id = "family",
        banglaName = "পারিবারিক",
        englishName = "Family",
        icon = Icons.Default.Bookmark,
        color = CatFamilyColor,
        defaultTags = listOf("এনআইডি", "পাসপোর্ট", "জন্ম সনদ", "স্কুল পেপার", "পরিচয়পত্র")
    ),
    HOME(
        id = "home",
        banglaName = "বাড়ি ও সম্পত্তি",
        englishName = "Home & Property",
        icon = Icons.Default.Home,
        color = CatHomeColor,
        defaultTags = listOf("বিদ্যুৎ বিল", "গ্যাস বিল", "পানি বিল", "ভাড়া চুক্তি", "মেরামত রসিদ")
    ),
    VEHICLE(
        id = "vehicle",
        banglaName = "যানবাহন",
        englishName = "Vehicle",
        icon = Icons.Default.DirectionsCar,
        color = CatVehicleColor,
        defaultTags = listOf("গাড়ির কাগজ", "ইন্স্যুরেন্স", "সার্ভিসিং", "ট্যাক্স টোকেন", "ফিটনেস")
    ),
    PERSONAL(
        id = "personal",
        banglaName = "ব্যক্তিগত ও ডায়েরি",
        englishName = "Personal & Diary",
        icon = Icons.Default.Person,
        color = CatPersonalColor,
        defaultTags = listOf("ডায়েরি", "বছরের লক্ষ্য", "পরিকল্পনা", "স্মৃতি", "ব্যক্তিগত নোট")
    ),
    EDUCATION(
        id = "education",
        banglaName = "শিক্ষা",
        englishName = "Education",
        icon = Icons.Default.School,
        color = CatEducationColor,
        defaultTags = listOf("সার্টিফিকেট", "মার্কশিট", "কোর্সের নোট", "ভর্তি", "অ্যাসাইনমেন্ট")
    ),
    EVENT(
        id = "event",
        banglaName = "স্মৃতি ও ইভেন্ট",
        englishName = "Events & Memories",
        icon = Icons.Default.Cake,
        color = CatEventColor,
        defaultTags = listOf("ভ্রমণ", "বিয়ে", "জন্মদিন", "উৎসব", "ফটোগ্রাফি")
    ),
    GENERAL(
        id = "general",
        banglaName = "সাধারণ",
        englishName = "General",
        icon = Icons.Default.Folder,
        color = Color(0xFF64748B),
        defaultTags = listOf("জরুরি", "আর্কাইভ", "অন্যান্য")
    );

    companion object {
        fun fromId(id: String): FileCategory {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: GENERAL
        }
    }
}
